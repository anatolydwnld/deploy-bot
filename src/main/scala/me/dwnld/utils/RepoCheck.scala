package me.dwnld.utils

import java.io.File
import java.time.Duration
import org.eclipse.jgit.api._
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.transport.RefSpec
import scala.collection.JavaConverters._
import scala.util.Try

case class RepoCheck(
  id: String,
  uri: String,
  privateKey: String,
  masterBranch: String = "master",
  developBranch: String = "develop",
  maxDeployInterval: Duration = Duration.ofDays(2)
) {
  val transportCallback = SshConfig.Callback(privateKey)
  def processRepo: RepoState = {
    val tmpDirRoot = System.getProperty("java.io.tmpdir")
    val repoRoot = new File(tmpDirRoot + File.separator + id)
    val git = if(repoRoot.exists()) {
      Git.open(repoRoot)
    } else {
      new CloneCommand()
        .setURI(uri)
        .setDirectory(repoRoot)
        .setTransportConfigCallback(transportCallback)
        .setBranch(masterBranchRefName)
        .setCloneAllBranches(false)
        .setBare(true)
        .call()
    }

    git.fetch()
      .setRefSpecs(new RefSpec(masterBranchRefName), new RefSpec(developBranchRefName))
      .setTransportConfigCallback(transportCallback)
      .call

    val devoRef = branchObjectId(developBranchRefName, git)
    val masterRef = branchObjectId(masterBranchRefName, git)

    val devoHead = git.log().add(devoRef).call.asScala.head
    val inDevo = git.log().addRange(masterRef, devoRef).call.asScala.filter { _.getParents.size == 1 }
    val inMaster = git.log().addRange(devoRef, masterRef).call.asScala.filter { _.getParents.size == 1 }
    //val lag = Duration.ofSeconds(devoHead.getCommitTime() - masterHead.getCommitTime())
    RepoState(Duration.ofSeconds(System.currentTimeMillis/1000 - devoHead.getCommitTime()), inDevo.size, inMaster.size)
  }

  val masterBranchRefName = s"refs/heads/${masterBranch}"
  val developBranchRefName = s"refs/heads/${developBranch}"

  private[this] def branchObjectId(refName: String, git: Git) = {
    git.getRepository().getRef(s"${refName}").getObjectId()
  }

}
