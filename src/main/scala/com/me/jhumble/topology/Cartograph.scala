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

trait Cartograph {
  val name: String
  val locations: Set[Place]
  def navigate(from: Int, in: Direction): Option[Place]
}
object Cartograph {

  def apply(bp: BluePrint): Option[Cartograph] = {
    val vertices: Set[Vertex] = bp.links.map(
      (link: (Int, String, Int)) => { // These are safe since we must have called validate on the bp
        val from = link._1
        val to = link._3
        val dir = Direction.fromString(link._2)
        Vertex(Node(from), dir, Node(to))
      }
    )
    val top: Try[Topology] = TopologyBuilder(vertices).build()
    top match {
      case Success(t) => Some(new CartographImpl(bp.name, bp.places, t))
      case Failure(_) => None
    }
  }
}
private final class CartographImpl(n: String, places: Map[Int, Place], top: Topology) extends Cartograph {
  val name: String = n
  val locations: Set[Place] = top.nodes().map(
    (n: Node) => places(n.id)
  )

  def navigate(from: Int, in: Direction): Option[Place] = {
    top.destination(Node(from), in).map(
      n => places(n.id)
    )
  }
}
