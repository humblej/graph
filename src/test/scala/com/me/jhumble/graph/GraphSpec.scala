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
  val nodeThree = new SimpleNode("Three")
  val nodeFour = new SimpleNode("Four")
  val nodeFive = new SimpleNode("Five")

  val v1 = Vertex(nodeOne, North, nodeTwo)
  val v2 = Vertex(nodeTwo, West, nodeThree)
  val v3 = Vertex(nodeThree, South, nodeFour)
  val v4 = Vertex(nodeFour, East, nodeOne)
  val v5 = Vertex(nodeFour, South, nodeFive)

  val v1a = Vertex(nodeOne, North, nodeThree)
  val v1r = Vertex(nodeTwo, South, nodeOne)
  val v1x = Vertex(nodeTwo, South, nodeThree)
  val v1z = Vertex(nodeOne, West, nodeTwo)

  "An empty graph" should "have no nodes" in {
    val builder = GraphBuilder()
    val graph = builder.build().get
    val nodes = graph.nodes()
    assert(nodes.size == 0)
  }

  "An empty graph" should "have not be navigable" in {
    val builder = GraphBuilder()
    val graph = builder.build().get
    val node = graph.move(nodeOne, North)
    assert(node == None)
  }

  "A graph with one vertex" should "have two nodes" in {
    val builder = GraphBuilder().add(v1)
    val graph = builder.build().get
    val nodes = graph.nodes()
    assert(nodes.size == 2)
    assert(nodes.diff(Set(nodeOne, nodeTwo)).size == 0)
  }

  "A graph with one vertex" should "be navigable using the correct direction" in {
    val builder = GraphBuilder().add(v1)
    val graph = builder.build().get
    val node = graph.move(nodeOne, North)
    assert(node == Some(nodeTwo))
  }

  "A graph with one vertex" should "be not be navigable in the wrong direction" in {
    val builder = GraphBuilder().add(v1)
    val graph = builder.build().get
    val node = graph.move(nodeOne, East)
    assert(node == None)
  }

  "A graph with reciprocal vertices" should "be navigable to and from" in {
    val builder = GraphBuilder().add(v1).add(v1r)
    val graph = builder.build().get
    val node = graph.move(nodeOne, North)
    val back = graph.move(node.get, South)
    assert(back == Some(nodeOne))
  }

  "A graph with a cycle" should "be navigable" in {
    val builder = GraphBuilder().add(v1).add(v2).add(v3).add(v4).add(v5)
    val graph = builder.build().get
    val res1 = graph.move(nodeOne, North)
    val res2 = graph.move(res1.get, West)
    val res3 = graph.move(res2.get, South)
    val res4 = graph.move(res3.get, East)

    assert(res4 == Some(nodeOne))
  }

  "A graph with a node with two vertices in the same direction" should "fail to build" in {
    val builder = GraphBuilder().add(v1).add(v1a)
    val graph = builder.build()
    assert(graph.isFailure)
  }

  "A graph with (X, North)->Y but (Y, South) ->Z" should "fail to build" in {
    val builder = GraphBuilder().add(v1).add(v1x)
    val graph = builder.build()
    assert(graph.isFailure)
  }

  "A node reaching the same destination using different directions" should "fail to build" in {
    val builder = GraphBuilder().add(v1).add(v1z)
    val graph = builder.build()
    assert(graph.isFailure)
  }
}
