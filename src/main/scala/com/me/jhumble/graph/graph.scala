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

package com.me.jhumble.graph

// Generalised node for a graph
trait Node {
  type T
  val element: T
}

// Possible inter-node directions
sealed trait Direction
case object North extends Direction
case object South extends Direction
case object East extends Direction
case object West extends Direction

// A connection between nodes
final case class Vertex(from: Node, direction: Direction, to: Node)

trait Graph {
  def move(n: Node, d: Direction): Option[Node]
  def nodes(): Set[Node]
}

// A builder for graphs
final class GraphBuilder(vs: Set[Vertex]) {
  def add(v: Vertex): GraphBuilder = {
    GraphBuilder(vs + v)
  }

  def build(): Graph = {
    GraphImpl(vs)
  }

}
object GraphBuilder {
  def apply(): GraphBuilder = {
    new GraphBuilder(Set())
  }
  def apply(vs: Set[Vertex]) = {
    new GraphBuilder(vs)
  }
}

private final class GraphImpl(vs: Set[Vertex]) extends Graph {
  def move(n: Node, d: Direction): Option[Node] = {
    val mtch = (v: Vertex) => (v.from == n && v.direction == d)
    vs.find(mtch).map(_.to)
  }
  def nodes(): Set[Node] = {
    val vnodes = (v: Vertex) => Set[Node]() + v.to + v.from
    vs.map(vnodes).flatten
  }
}
object GraphImpl {
  def apply(vs: Set[Vertex]): Graph = {
    new GraphImpl(vs)
  }
}