package models

import org.eclipse.jgit.lib.ObjectId
import repositories.{ ParsingError, RzError }

import java.io.InputStream
import java.time.{ LocalDateTime, ZoneId }

case class RzRepository(
  owner: Account,
  name: String,
  entrypoint: Option[String],
  createdAt: Long,
  updatedAt: Long
) {
  def id: String = IdTable.rzRepoPrefix + owner.userName + ":" + name

  def collaboratorsListId: String = IdTable.rzRepoCollaboratorsPrefix + owner.userName + ":" + name

  def httpUrl(request: RepositoryRequestHeader): String = s"https://${request.host}/${owner.userName}/${name}.git"

  def sshUrl(request: RepositoryRequestHeader): String = s"git@${request.host}:${owner.userName}/${name}.git"

  def toMap = Map("entrypoint" -> entrypoint, "createdAt" -> createdAt, "updatedAt" -> updatedAt)

  def this(owner: Account, name: String) = this(owner, name, None, DateTime.now, DateTime.now)
}

object RzRepository {
  def id(owner: String, name: String): String = IdTable.rzRepoPrefix + owner + ":" + name

  val defaultBranch = "master"

  def parseId(id: String): (String, String) = {
    val s = id.split(":")
    (s(1), s(2))
  }

  def make(name: String, owner: Account, data: Map[String, String]): Either[RzError, RzRepository] =
    (for {
      createdAt <- data.get("createdAt")
      updatedAt <- data.get("updatedAt")
    } yield RzRepository(
      owner,
      name,
      data.get("entrypoint"),
      DateTime.parseTimestamp(createdAt),
      DateTime.parseTimestamp(updatedAt)
    )) match {
      case Some(a) => Right(a)
      case None    => Left(ParsingError)
    }
}

/**
 * The file data for the file list of the repository viewer.
 *
 */
case class FileInfo(
  id: ObjectId,
  isDirectory: Boolean,
  name: String,
  path: String,
  message: String,
  commitId: String,
  author: String,
  mailAddress: String
)

/**
 * The file content data for the file content view of the repository viewer.
 *
 * @param viewType "image", "large" or "other"
 * @param size     total size of object in bytes
 * @param content  the string content
 * @param charset  the character encoding
 */
case class ContentInfo(viewType: String, size: Option[Long], content: Option[String], charset: Option[String]) {

  /**
   * the line separator of this content ("LF" or "CRLF")
   */
  lazy val lineSeparator: String = if (content.exists(_.indexOf("\r\n") >= 0)) "CRLF" else "LF"
}

sealed trait RepositoryTreeContent

case class Blob(
  content: ContentInfo,
  latestCommit: CommitInfo,
  isLfsFile: Boolean
) extends RepositoryTreeContent

case object EmptyBlob extends RepositoryTreeContent

case object EmptyRepository extends RepositoryTreeContent

case class RawFile(inputStream: InputStream, contentLength: Integer, contentType: String)

/**
 * The commit data.
 *
 */
case class CommitInfo(
  id: String,
  shortMessage: String,
  fullMessage: String,
  parents: List[String],
  authorName: String,
  authorEmailAddress: String,
  commitTime: LocalDateTime,
  committerName: String,
  committerEmailAddress: String
) {
  def this(rev: org.eclipse.jgit.revwalk.RevCommit) =
    this(
      rev.getName,
      rev.getShortMessage,
      rev.getFullMessage,
      rev.getParents.map(_.name).toList,
      rev.getAuthorIdent.getName,
      rev.getAuthorIdent.getEmailAddress,
      rev.getCommitterIdent.getWhen.toInstant.atZone(ZoneId.systemDefault()).toLocalDateTime,
      rev.getCommitterIdent.getName,
      rev.getCommitterIdent.getEmailAddress
    )
}
