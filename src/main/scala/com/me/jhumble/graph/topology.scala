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
import scala.util.Try

// Generalised node for a graph
trait Node {
  type T
  val element: T
}

// Possible inter-node directions
sealed trait Direction {
  val opposite: Direction
}
case object North extends Direction {
  val opposite: Direction = South
}
case object South extends Direction {
  val opposite: Direction = North
}
case object East extends Direction {
  val opposite: Direction = West
}
case object West extends Direction {
  val opposite: Direction = East
}

// A connection between nodes
final case class Vertex(from: Node, direction: Direction, to: Node)

trait Topology {
  def destination(from: Node, in: Direction): Option[Node]
  def nodes(): Set[Node]
}

// A builder for graphs
final class TopologyBuilder(vs: Set[Vertex]) {

  def add(v: Vertex): TopologyBuilder = {
    TopologyBuilder(vs + v)
  }

  def build(): Try[Topology] = {
    Try(
      TopologyImpl(vs)
    )
  }

}
object TopologyBuilder {
  def apply(): TopologyBuilder = {
    new TopologyBuilder(Set())
  }
  def apply(vs: Set[Vertex]): TopologyBuilder = {
    new TopologyBuilder(vs)
  }
}

private final class TopologyImpl(vs: Set[Vertex]) extends Topology {
  def destination(from: Node, in: Direction): Option[Node] = {
    val mtch = (v: Vertex) => (v.from == from && v.direction == in)
    vs.find(mtch).map(_.to)
  }
  def nodes(): Set[Node] = {
    val vnodes = (v: Vertex) => Set[Node]() + v.to + v.from
    vs.map(vnodes).flatten
  }
}
object TopologyImpl {
  def apply(vs: Set[Vertex]): Topology = {
    validate(vs)
    new TopologyImpl(vs)
  }

  def validate(vs: Set[Vertex]): Unit = {
    if (illegalVertices(vs) || illegalCombinations(vs)) {
      throw new IllegalArgumentException("Inconsistent Topology")
    }
  }

  def illegalVertices(vs: Set[Vertex]) = {
    vs.exists(loopingVertex)
  }

  def illegalCombinations(vs: Set[Vertex]): Boolean = {
    val vertexCombinations = vs.toList.combinations(2)
    val illegalVertex = (vs: List[Vertex]) => {
      val List(v1, v2) = vs
      duplicateVertex(v1, v2) || contradictoryOpposites(v1, v2) || contradictoryVertices(v1, v2)
    }
    vertexCombinations.exists(illegalVertex)
  }

  // Same starting node and direction
  def duplicateVertex(v1: Vertex, v2: Vertex): Boolean = {
    (v1.from == v2.from) && (v1.direction == v2.direction)
  }

  // e.g. going North then South doesn't bring you back to the same place.
  def contradictoryOpposites(v1: Vertex, v2: Vertex): Boolean = {
    val Vertex(from, dir, end) = v1
    v2.from == end && v2.direction == v1.direction.opposite && v2.to != from
  }

  // Going in different directions from the same place leads to the same destination.
  def contradictoryVertices(v1: Vertex, v2: Vertex): Boolean = {
    val Vertex(from, dir, end) = v1
    v2.from == from && v2.direction != v1.direction && v2.to == end
  }

  // Vertext that starts and ends in the same place.
  def loopingVertex(v1: Vertex): Boolean = {
    v1.from == v1.to
  }
}
