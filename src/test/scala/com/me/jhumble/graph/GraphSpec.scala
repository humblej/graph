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

import org.scalatest.FlatSpec

class GraphSpec extends FlatSpec {

  class SimpleNode(e: String) extends Node {
    type T = String
    val element: String = e
  }

  val nodeOne = new SimpleNode("One")
  val nodeTwo = new SimpleNode("Two")
  val v1 = Vertex(nodeOne, North, nodeTwo)
  val v2 = Vertex(nodeTwo, South, nodeOne)

  "An empty graph" should "have no nodes" in {
    val builder = GraphBuilder()
    val graph = builder.build()
    val nodes = graph.nodes()
    assert(nodes.size == 0)
  }

  "A graph with one vertex" should "have two nodes" in {
    val builder = GraphBuilder().add(v1)
    val graph = builder.build()
    val nodes = graph.nodes()
    assert(nodes.size == 2)
    assert(nodes.diff(Set(nodeOne, nodeTwo)).size == 0)
  }

  "A graph with one vertex" should "be navigable" in {
    val builder = GraphBuilder().add(v1)
    val graph = builder.build()
    val node = graph.move(nodeOne, North)
    assert(node == Some(nodeTwo))
  }

  "A graph with reciprocal vertices" should "be navigable to and from" in {
    val builder = GraphBuilder().add(v1).add(v2)
    val graph = builder.build()
    val node = graph.move(nodeOne, North)
    val back = grapg.move(node, South)
    assert(node == Some(nodeOne))
  }

}
