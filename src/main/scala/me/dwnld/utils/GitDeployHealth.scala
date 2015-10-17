package me.dwnld.utils

import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import java.io.File
import slack.SlackUtil
import slack.rtm.SlackRtmClient
import akka.actor.ActorSystem

object GitDeployHealth extends App {
  implicit val system = ActorSystem("deploy-bot")
  implicit val ec = system.dispatcher
  val token = system.settings.config.getString("slack-token")
  val client = SlackRtmClient(token)

  try {
    val selfId = client.state.self.id

    client.onMessage { message =>
      val mentionedIds = SlackUtil.extractMentionedIds(message.text)
      if(mentionedIds.contains(selfId)) {
        if(message.text.contains("shutdown")) {
          system.shutdown()
        }

        if(message.text.contains("stale")) {
          client.sendMessage(message.channel, s"Checking on the state of things")
          val allGood = Checks().foldLeft(true) { (prev, check) =>
            val state = check.processRepo
            if(state.behindBy > 0) {
              client.sendMessage(message.channel,
                s"${check.id} has ${state.behindBy} deployed changes that are not in develop. Bad times")
              false
            } else if(state.aheadBy > 0 && check.maxDeployInterval.compareTo(state.lag) < 0) {
              client.sendMessage(message.channel,
                s"${check.id} has undeployed changes older than ${state.lag.toDays} days. Please, deploy")
              false
            } else {
              true && prev
            }
          }

          if(allGood) client.sendMessage(message.channel, "All is well")
        }
      }
    }
    system.awaitTermination()
  } finally {
    client.close()
    system.shutdown()
    system.awaitTermination()
  }
}
