/*
 * Copyright 2016 Jon Humble
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

package com.me.jhumble.topology

import scala.util._
import org.json4s._
import org.json4s.native.JsonMethods._

// Case classes for parsing the JSON files.
final case class Route(direction: String, destination: Int)
final case class Place(id: Int, view: String, description: String, routes: List[Route])
final case class World(name: String, places: List[Place])

// Internal representation of a file
final case class BluePrint(name: String, places: Map[Int, Place], links: Set[(Int, String, Int)]) {
  // Make sure each end of the links is a key in the places map and that the direction is valid.
  def validate(): Boolean = {
    links.forall(
      (link: (Int, String, Int)) => {
        places.contains(link._1) && places.contains(link._3) && Direction.fromString.isDefinedAt(link._2)
      }
    )
  }
}

final object Parser {
  def parseWorld(json: String): Either[String, BluePrint] = {
    implicit val formats = DefaultFormats
    val parsed = parse(json)
    Try(parsed.extract[World]).map(worldToBluePrint) match {
      case Success(bp) => bp.validate() match {
        case true  => Right(bp)
        case false => Left("BluePrint failed to validate (bad node id in links or bad direction)")
      }
      case Failure(xs) => Left(xs.getLocalizedMessage())
    }
  }

  def worldToBluePrint(world: World): BluePrint = {

    val locationIndex = world.places.map(
      (p: Place) => (p.id, p)
    )

    val connections = world.places.map(
      (p: Place) => p.routes.map(
        (r: Route) => (p.id, r.direction, r.destination)
      )
    ).flatten.toSet

    BluePrint(world.name, locationIndex.toMap, connections)
  }
}
