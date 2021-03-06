package controllers

import actions.AuthenticatedAction
import akka.actor.ActorSystem
import models._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{ Millis, Seconds, Span }
import org.scalatest.{ BeforeAndAfterAll, PrivateMethodTester }
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.{ Cookie, Result }
import play.api.test.CSRFTokenHelper.addCSRFToken
import play.api.test.Helpers.{ await, defaultAwaitTimeout }
import play.api.test.{ FakeRequest, Injecting }
import play.api.{ Configuration, Logger }
import repositories.{ AccountRepository, GitRepository, RzMetaGitRepository }

case class AuthorizedAccount(a: Account, s: (String, String), c: Cookie)

class GenericControllerTest
    extends PlaySpec
    with BeforeAndAfterAll
    with GuiceOneAppPerSuite
    with Injecting
    with PrivateMethodTester
    with ScalaFutures {
  val logger: Logger = play.api.Logger(this.getClass)

  implicit val sys: ActorSystem = ActorSystem("RepositoryTest")
  implicit val defaultPatience: PatienceConfig =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(500, Millis))

  def getRandomString: String =
    java.util.UUID.randomUUID.toString.replace("-", "")

  def config: Configuration = app.injector.instanceOf[Configuration]

  def accountController: AccountController             = app.injector.instanceOf[AccountController]
  def collaboratorsController: CollaboratorsController = app.injector.instanceOf[CollaboratorsController]
  def fileTreeController: FileTreeController           = app.injector.instanceOf[FileTreeController]

  def accountRepository: AccountRepository = app.injector.instanceOf[AccountRepository]
  def rzGitRepository: RzMetaGitRepository = app.injector.instanceOf[RzMetaGitRepository]

  def gitEntitiesController: RzRepositoryController = app.injector.instanceOf[RzRepositoryController]
  def git: GitRepository                            = app.injector.instanceOf[GitRepository]

  def authAction: AuthenticatedAction = app.injector.instanceOf[AuthenticatedAction]

  val sessionName: String = SessionName.toString

  def createAccount(): AuthorizedAccount = {
    val data =
      AccountRegistrationData(getRandomString, Some(getRandomString), getRandomString, s"$getRandomString@rzm.dev")
    val request = addCSRFToken(
      FakeRequest(routes.AccountController.saveAccount())
        .withFormUrlEncodedBody(
          "userName"    -> data.userName,
          "fullName"    -> data.fullName.get,
          "password"    -> data.password,
          "mailAddress" -> data.email
        )
    )
    await(accountController.saveAccount().apply(request))

    val (sessionId, cookie) = await(authAction.createSession(UserInfo(data.userName)))
    val session             = (Auth.SESSION_ID -> sessionId)
    AuthorizedAccount(new Account(data), session, cookie)
  }

  def createRepository(
    name: String,
    owner: AuthorizedAccount
  ): (Result, RzRepository) = {
    val request = addCSRFToken(
      FakeRequest(routes.RzRepositoryController.saveRepository())
        .withFormUrlEncodedBody("name" -> name, "description" -> getRandomString)
        .withSession(owner.s)
        .withCookies(owner.c)
    )
    val result = await(gitEntitiesController.saveRepository().apply(request))
    (result, new RzRepository(owner.a, name))
  }

}
