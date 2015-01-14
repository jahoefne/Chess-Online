import controllers._
import java.lang.reflect.Constructor
import play.api.mvc.WithFilters
import play.filters.gzip.GzipFilter
import securesocial.core.RuntimeEnvironment
import securesocial.core.providers.{UsernamePasswordProvider, GoogleProvider}

import scala.collection.immutable.ListMap


/**
 * Global Settings used for SecureSocial
 */
object Global extends WithFilters(new GzipFilter()) with play.api.GlobalSettings {

  object MyRuntimeEnvironment extends RuntimeEnvironment.Default[User] {
    override lazy val userService =  DBUserService

    override lazy val providers = ListMap(
      include(new GoogleProvider(routes, cacheService, oauth2ClientFor(GoogleProvider.Google))),
      include(new UsernamePasswordProvider[User](userService, avatarService, viewTemplates, passwordHashers))
    )
  }

  override def getControllerInstance[A](controllerClass: Class[A]): A = {
    val instance  = controllerClass.getConstructors.find { c =>
      val params = c.getParameterTypes
      params.length == 1 && params(0) == classOf[RuntimeEnvironment[User]]
    }.map {
        _.asInstanceOf[Constructor[A]].newInstance(MyRuntimeEnvironment)

    }
    instance.getOrElse(super.getControllerInstance(controllerClass))
  }
}
