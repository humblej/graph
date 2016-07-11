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
import scala.collection._

case class Location(element: Place) extends Node { type T = Place }

trait Map {
  val name: String
  val locations: immutable.Set[Location]
  def navigate(from: Location, in: Direction): Option[Location]
}
object Map {

  def apply(bp: BluePrint): Option[Map] = {
    val vertices: immutable.Set[Vertex] = bp.links.map(
      (link: (Int, String, Int)) => { // These are safe since we must have called validate on the bp
        val from = bp.places(link._1)
        val to = bp.places(link._3)
        val dir = Direction.fromString(link._2)
        Vertex(Location(from), dir, Location(to))
      }
    )
    val top: Try[Topology] = TopologyBuilder(vertices).build()
    top match {
      case Success(t) => Some(new MapImpl(bp.name, t.asInstanceOf[MapTopology]))
      case Failure(_) => None
    }
  }
}
private final class MapImpl(n: String, top: MapTopology) extends Map {
  val name: String = n
  val locations: immutable.Set[Location] = top.nodes().map(_.asInstanceOf[Location])

  def navigate(from: Location, in: Direction): Option[Location] = {
    top.destination(from, in).map(x => x.asInstanceOf[Location])
  }
}
