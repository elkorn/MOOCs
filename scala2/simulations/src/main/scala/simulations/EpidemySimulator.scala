package simulations

import math.random
import scala.annotation.tailrec

class EpidemySimulator extends Simulator {

  def randomBelow(i: Int) = (random * i).toInt

  object Movement extends Enumeration {
    type Movement = Value
    val Up, Down, Left, Right = Value
    def getRandom(): Movement = Movement.Value(randomBelow(4))
  }

  protected[simulations] object SimConfig {
    val population: Int = 500
    val roomRows: Int = 8
    val roomColumns: Int = 8
    val prevalenceRate: Double = 0.01
    val incubationTime = 6
    val dieTime = 14
    val immuneTime = 16
    val healTime = 18
    def moveDelay() = randomBelow(5)
  }

  import SimConfig._
  import Movement._

  val persons: List[Person] = for (n <- List.range(0, population)) yield new Person(n)

  def bounded(value: Int, bound: Int): Int =
    if (value == 0) value
    else math.abs(value % bound)

  private def performInitialInfection() {
    val expectedInfections = population * prevalenceRate
    @tailrec
    def infectionStep(alreadyInfected: Int) {
      //      println("Infected: " + alreadyInfected)
      if (alreadyInfected != expectedInfections) {
        val person = persons.drop(randomBelow(population - 1)).head
        if (person.infected) infectionStep(alreadyInfected)
        else {
          person.infected = true
          infectionStep(alreadyInfected + 1)
        }
      }
    }

    infectionStep(0)
  }

  private def isVisiblyInfected(person: Person): Boolean = person.sick || person.dead
  private def isInfectious(person: Person): Boolean = person.sick || person.dead || person.infected || person.immune

  private def getPeopleWithCondition(room: Pair[Int, Int], condition: Person => Boolean): List[Person] = {
    persons.filter { person =>
      //      println(s"Person dead: ${person.dead} sick: ${person.sick} immune: ${person.immune}")
      (person.row == room._1 && person.col == room._2) && condition(person) == true
    }
  }

  private def containsPeopleWithCondition(room: Pair[Int, Int], condition: Person => Boolean): Boolean =
    getPeopleWithCondition(room, condition).length > 0

  private def containsVisiblyInfectedPeople(room: Pair[Int, Int]): Boolean = {
    containsPeopleWithCondition(room, isVisiblyInfected)
  }

  private def containsInfectiousPeople(room: Pair[Int, Int]): Boolean =
    containsPeopleWithCondition(room, isInfectious)

  private def getInfectiousPeople(room: Pair[Int, Int]): List[Person] = getPeopleWithCondition(room, isInfectious)

  performInitialInfection()

  class Person(val id: Int) {
    var infected = false
    var sick = false
    var immune = false
    var dead = false

    // demonstrates random number generation
    var row: Int = randomBelow(roomRows)
    var col: Int = randomBelow(roomColumns)

    def calculatePosition(movement: Movement): Pair[Int, Int] = {
      movement match {
        case Up => new Pair(bounded(row - 1, roomRows), col)
        case Down => new Pair(bounded(row + 1, roomRows), col)
        case Left => new Pair(row, bounded(col - 1, roomColumns))
        case Right => new Pair(row, bounded(col - 1, roomColumns))
      }
    }

    def setPosition(newPosition: Pair[Int, Int]) {
      row = newPosition._1
      col = newPosition._2
    }

    private def becomeSick() = {
      //      println("getting sick")
      sick = true
    }

    private def becomeDead() {
      dead = true
    }

    private def becomeImmune() {
      immune = true
    }

    private def becomeHealthy() {
      sick = false
      immune = false
      infected = false
    }

    def beginDying() {
      afterDelay(dieTime)(probablyDie)
      def probablyDie() {
        if (randomBelow(4) == 0) becomeDead() // 0.25 death probability
      }
    }

    def beginBecomingImmune() {
      afterDelay(immuneTime)(if (!dead) (becomeImmune))
    }

    def beginBecomingSick() {
      afterDelay(incubationTime)(if (!dead) (becomeSick))
    }

    def beginGettingHealthy() {
      afterDelay(healTime)(if (!dead) (becomeHealthy))
    }

    def becomeInfected() {
      afterDelay(0) {
        infected = true
        beginBecomingSick()
        beginDying()
        beginBecomingImmune()
        beginGettingHealthy()
      }
    }

    def handleInfectionFromOthers(infectiousPeople: List[Person]) {
      for (ip <- infectiousPeople if !infected) {
        if (randomBelow(10) <= 4) becomeInfected() // for transmission rate of 0.4
      }
    }

    def handleInfectionForRoom(room: Pair[Int, Int]) {
      val infectiousPeople = getInfectiousPeople(room)
      //              println("There are " + infectiousPeople.length + " here.")
      if (!(immune || infected)) {
        handleInfectionFromOthers(infectiousPeople)
      }
    }

    def moveByFeet(): Pair[Int, Int] = {
      // movements that do not lead to infected rooms
      val cleanMovements = for {
        m <- Movement.values if !containsVisiblyInfectedPeople(calculatePosition(m))
      } yield m

      cleanMovements match {
        case Movement.ValueSet.empty => { Pair(row, col) }
        case _ => {
          calculatePosition(cleanMovements.drop(randomBelow(cleanMovements.size - 1)).head)
        }
      }
    }

    def moveByAirplane(): Pair[Int, Int] = {
      new Pair(randomBelow(roomRows), randomBelow(roomColumns))
    }

    def move() {
      afterDelay(moveDelay()) {
        if (!dead) {
          val newRoom = if (randomBelow(100) == 0) moveByAirplane() else moveByFeet()
//          val newRoom = moveByFeet()
//          val newRoom = moveByAirplane()
          setPosition(newRoom)
          // Infection and stuff...
          handleInfectionForRoom(newRoom)
          move() // After each move (and also after the beginning of the simulation), a person moves
        }
      }
    }

    // Bootstrap for simulation startup
    move()
  }
}
