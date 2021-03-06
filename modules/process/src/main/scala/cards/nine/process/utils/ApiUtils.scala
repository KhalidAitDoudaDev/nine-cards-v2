/*
 * Copyright 2017 47 Degrees, LLC. <http://www.47deg.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cards.nine.process.utils

import cards.nine.commons.NineCardExtensions._
import cards.nine.commons.contexts.ContextSupport
import cards.nine.commons.services.TaskService
import cards.nine.commons.services.TaskService._
import cards.nine.models.{RequestConfig, User}
import cards.nine.services.api.{ApiServiceException, ImplicitsApiServiceExceptions}
import cards.nine.services.persistence.PersistenceServices
import cats.syntax.either._
import monix.eval.Task

class ApiUtils(persistenceServices: PersistenceServices) extends ImplicitsApiServiceExceptions {

  def getRequestConfig(implicit context: ContextSupport): TaskService[RequestConfig] = {

    def loadUser(userId: Int): TaskService[RequestConfig] =
      (for {
        user <- persistenceServices
          .findUserById(userId)
          .resolveOption(s"Can't find the user with id $userId")
        keys <- loadTokens(user)
        (apiKey, sessionToken) = keys
        androidId <- persistenceServices.getAndroidId
      } yield RequestConfig(apiKey, sessionToken, androidId, user.marketToken))
        .resolve[ApiServiceException]

    def loadTokens(user: User): TaskService[(String, String)] =
      (user.apiKey, user.sessionToken) match {
        case (Some(apiKey), Some(sessionToken)) =>
          TaskService(Task(Either.right(apiKey, sessionToken)))
        case _ =>
          TaskService(Task(Either.left(ApiServiceException("Session token doesn't exists"))))
      }

    context.getActiveUserId match {
      case Some(id) => loadUser(id)
      case None     => TaskService(Task(Either.left(ApiServiceException("Missing user id"))))
    }

  }

}
