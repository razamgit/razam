package models

import org.eclipse.jgit.revwalk.RevCommit

import java.io.File

// Forms related to auth

case class AccountRegistrationData(userName: String, fullName: Option[String], password: String, email: String)

case class AccountData(userName: String, fullName: Option[String], email: String)

case class RepositoryData(name: String, description: Option[String])

case class PasswordData(oldPassword: String, newPassword: String)

case class AccountLoginData(userName: String, password: String)

// Forms related to project control

case class CommitFile(id: String, name: String, file: File)

case class EditedItem(content: String, rev: String, path: String, name: String)

case class UploadFileForm(path: String)

case class RepositoryGitData(files: List[FileInfo], lastCommit: Option[RevCommit])

case class NewItem(name: String, rev: String, path: String, isFolder: Boolean)

case class SshKeyData(publicKey: String)

case class SshRemoveData(id: String)

case class NewCollaboratorData(emailOrLogin: String, accessLevel: String)

case class RemoveCollaboratorData(id: String)

// Templates

case class TemplateData(tplName: String)