package com.assignment.helper

object MarkGenerator {
  private val markGenerator = scala.util.Random

  def generateMark = markGenerator.nextInt()
}
