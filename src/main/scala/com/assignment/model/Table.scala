package com.assignment.model

import com.assignment.helper.MarkGenerator

// Оптимистичная блокировка будет достигаться за счет метки, слепок данных мне ни к чему.
// Используется обычный целочисленный тип, но для усложнения можно взять несколько timestamp или несколько хешей от объекта.
// Идеально подойдет ObjectId из монго, 12 байт и все такое.
case class Table(name: String, participants: Int) {
  val mark: Int = MarkGenerator.generateMark
}
