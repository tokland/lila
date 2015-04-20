package lila.tournament

import akka.actor._
import akka.pattern.{ ask, pipe }

import actorApi._
import lila.game.actorApi.FinishGame
import lila.hub.actorApi.map.Tell
import lila.hub.actorApi.WithUserIds
import makeTimeout.short

private[tournament] final class Organizer(
    api: TournamentApi,
    reminder: ActorRef,
    isOnline: String => Boolean,
    socketHub: ActorRef) extends Actor {

  context.system.lilaBus.subscribe(self, 'finishGame, 'adjustCheater)

  def receive = {

    case AllCreatedTournaments => TournamentRepo.allCreated foreach {
      _ foreach { tour =>
        tour.schedule match {
          case None =>
            if (tour.isEmpty) api wipeEmpty tour
            else if (tour.enoughPlayersToStart) api startIfReady tour
            else ejectLeavers(tour)
          case Some(schedule) =>
            if (schedule.at.isBeforeNow) api startScheduled tour
            else ejectLeavers(tour)
        }
      }
    }

    case StartedTournaments => TournamentRepo.started foreach { tours =>
      tours foreach { tour =>
        if (tour.readyToFinish) api finish tour
        else startPairing(tour)
      }
      reminder ! RemindTournaments(tours)
    }

    case FinishGame(game, _, _)                    => api finishGame game

    case lila.hub.actorApi.mod.MarkCheater(userId) => api ejectCheater userId
  }

  private def ejectLeavers(tour: Created) =
    tour.userIds filterNot isOnline foreach { api.withdraw(tour, _) }

  private def startPairing(tour: Started) {
    if (!tour.isAlmostFinished) {
      withWaitingUserIds(tour) { ids =>
        val users = tour.activeUserIds intersect ids
        tour.system.pairingSystem.createPairings(tour, users) onSuccess {
          case (pairings, events) =>
            pairings.toNel foreach { api.makePairings(tour, _, events) }
        }
      }
    }
  }

  private def withWaitingUserIds(tour: Tournament)(f: List[String] => Unit) {
    socketHub ! Tell(tour.id,
      (tour.pairings.isEmpty.fold(WithUserIds.apply _, WithWaitingUserIds.apply _)) {
        ids => f(ids.toList)
      }
    )
  }
}
