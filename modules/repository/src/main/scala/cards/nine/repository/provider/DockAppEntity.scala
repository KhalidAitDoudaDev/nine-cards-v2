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

package cards.nine.repository.provider

import android.database.Cursor
import cards.nine.repository.model.DockApp
import cards.nine.repository.Conversions._

case class DockAppEntity(id: Int, data: DockAppEntityData)

case class DockAppEntityData(
    name: String,
    dockType: String,
    intent: String,
    imagePath: String,
    position: Int)

object DockAppEntity {
  val table     = "DockApp"
  val name      = "name"
  val dockType  = "dockType"
  val intent    = "intent"
  val imagePath = "imagePath"
  val position  = "position"

  val allFields = Seq[String](NineCardsSqlHelper.id, name, dockType, intent, imagePath, position)

  def dockAppEntityFromCursor(cursor: Cursor): DockAppEntity =
    DockAppEntity(
      id = cursor.getInt(cursor.getColumnIndex(NineCardsSqlHelper.id)),
      data = DockAppEntityData(
        name = cursor.getString(cursor.getColumnIndex(name)),
        dockType = cursor.getString(cursor.getColumnIndex(dockType)),
        intent = cursor.getString(cursor.getColumnIndex(intent)),
        imagePath = cursor.getString(cursor.getColumnIndex(imagePath)),
        position = cursor.getInt(cursor.getColumnIndex(position))))

  def dockAppFromCursor(cursor: Cursor): DockApp = toDockApp(dockAppEntityFromCursor(cursor))

  def createTableSQL: String =
    s"""CREATE TABLE ${DockAppEntity.table}
        |(${NineCardsSqlHelper.id} INTEGER PRIMARY KEY AUTOINCREMENT,
        |${DockAppEntity.name} TEXT not null,
        |${DockAppEntity.dockType} TEXT not null,
        |${DockAppEntity.intent} TEXT not null,
        |${DockAppEntity.imagePath} TEXT not null,
        |${DockAppEntity.position} INTEGER not null)""".stripMargin
}
