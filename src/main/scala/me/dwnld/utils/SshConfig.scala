package me.dwnld.utils

import org.eclipse.jgit.api.TransportConfigCallback
import org.eclipse.jgit.transport._
import org.eclipse.jgit.transport.OpenSshConfig.Host
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import org.eclipse.jgit.util.FS

object SshConfig {
  case class Callback(privateKey: String) extends TransportConfigCallback {
    override def configure(transport: Transport){
      for(sshTransport <- Option(transport.asInstanceOf[SshTransport])) {
        sshTransport.setSshSessionFactory(new JschConfigSessionFactory{
          override def createDefaultJSch(fs: FS): JSch = {
            val ssh = super.createDefaultJSch(fs)
            ssh.addIdentity(privateKey)
            ssh
          }
          override def configure(h: Host, session: Session) {}
        })
      }
    }
  }
}
