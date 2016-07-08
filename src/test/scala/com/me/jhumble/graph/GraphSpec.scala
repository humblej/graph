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

import org.scalatest.FlatSpec

class TopologySpec extends FlatSpec {

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
  val vloop = Vertex(nodeOne, West, nodeOne)

  "An empty Topology" should "have no nodes" in {
    val builder = TopologyBuilder()
    val Topology = builder.build().get
    val nodes = Topology.nodes()
    assert(nodes.size == 0)
  }

  "An empty Topology" should "have not be navigable" in {
    val builder = TopologyBuilder()
    val Topology = builder.build().get
    val node = Topology.move(nodeOne, North)
    assert(node == None)
  }

  "A Topology with one vertex" should "have two nodes" in {
    val builder = TopologyBuilder().add(v1)
    val Topology = builder.build().get
    val nodes = Topology.nodes()
    assert(nodes.size == 2)
    assert(nodes.diff(Set(nodeOne, nodeTwo)).size == 0)
  }

  "A Topology with one vertex" should "be navigable using the correct direction" in {
    val builder = TopologyBuilder().add(v1)
    val Topology = builder.build().get
    val node = Topology.move(nodeOne, North)
    assert(node == Some(nodeTwo))
  }

  "A Topology with one vertex" should "be not be navigable in the wrong direction" in {
    val builder = TopologyBuilder().add(v1)
    val Topology = builder.build().get
    val node = Topology.move(nodeOne, East)
    assert(node == None)
  }

  "A Topology with reciprocal vertices" should "be navigable to and from" in {
    val builder = TopologyBuilder().add(v1).add(v1r)
    val Topology = builder.build().get
    val node = Topology.move(nodeOne, North)
    val back = Topology.move(node.get, South)
    assert(back == Some(nodeOne))
  }

  "A Topology with a cycle" should "be navigable" in {
    val builder = TopologyBuilder().add(v1).add(v2).add(v3).add(v4).add(v5)
    val Topology = builder.build().get
    val res1 = Topology.move(nodeOne, North)
    val res2 = Topology.move(res1.get, West)
    val res3 = Topology.move(res2.get, South)
    val res4 = Topology.move(res3.get, East)

    assert(res4 == Some(nodeOne))
  }

  "A Topology with a node with two vertices in the same direction" should "fail to build" in {
    val builder = TopologyBuilder().add(v1).add(v1a)
    val Topology = builder.build()
    assert(Topology.isFailure)
  }

  "A Topology with (X, North)->Y but (Y, South) ->Z" should "fail to build" in {
    val builder = TopologyBuilder().add(v1).add(v1x)
    val Topology = builder.build()
    assert(Topology.isFailure)
  }

  "A node reaching the same destination using different directions" should "fail to build" in {
    val builder = TopologyBuilder().add(v1).add(v1z)
    val Topology = builder.build()
    assert(Topology.isFailure)
  }

  "A Topology with a vertex loop" should "fail to build" in {
    val builder = TopologyBuilder().add(v1).add(vloop)
    val Topology = builder.build()
    assert(Topology.isFailure)
  }
}
