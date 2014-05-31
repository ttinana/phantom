/*
 * Copyright 2013 newzly ltd.
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
package com.newzly.phantom.query

import com.datastax.driver.core.querybuilder.{ Assignment, QueryBuilder, Update, Using }
import com.newzly.phantom.CassandraTable

class AssignmentsQuery[T <: CassandraTable[T, R], R](table: T, val qb: Update.Assignments) extends CQLQuery[AssignmentsQuery[T, R]] {

  def onlyIf[RR](condition: T => SecondaryQueryCondition): ConditionalUpdateQuery[T, R] = {
    new ConditionalUpdateQuery[T, R](table, qb.onlyIf(condition(table).clause))
  }

  def modify(a: T => Assignment): AssignmentsQuery[T, R] = {
    new AssignmentsQuery[T, R](table, qb.and(a(table)))
  }

  def and = modify _
}

class AssignmentOptionQuery[T <: CassandraTable[T, R], R](table: T, val qb: Update.Options) extends CQLQuery[AssignmentOptionQuery[T, R]] {

  def ttl(seconds: Int): AssignmentOptionQuery[T, R] = {
    new AssignmentOptionQuery[T, R](table, qb.and(QueryBuilder.ttl(seconds)))
  }

  def using(u: Using): AssignmentOptionQuery[T, R] = {
    new AssignmentOptionQuery[T, R](table, qb.and(u))
  }
}

class ConditionalUpdateQuery[T <: CassandraTable[T, R], R](table: T, val qb: Update.Conditions) extends CQLQuery[ConditionalUpdateQuery[T, R]] {

  def and[RR](condition: T => SecondaryQueryCondition): ConditionalUpdateQuery[T, R] = {
    new ConditionalUpdateQuery[T, R](table, qb.and(condition(table).clause))
  }
}

class ConditionalUpdateWhereQuery[T <: CassandraTable[T, R], R](table: T, val qb: Update.Conditions) extends CQLQuery[ConditionalUpdateWhereQuery[T, R]] {

  def and[RR](condition: T => SecondaryQueryCondition): ConditionalUpdateQuery[T, R] = {
    new ConditionalUpdateQuery[T, R](table, qb.and(condition(table).clause))
  }
}

class UpdateQuery[T <: CassandraTable[T, R], R](table: T, val qb: Update) extends CQLQuery[UpdateQuery[T, R]] {

  def onlyIf[RR](condition: T => SecondaryQueryCondition): ConditionalUpdateQuery[T, R] = {
    new ConditionalUpdateQuery[T, R](table, qb.onlyIf(condition(table).clause))
  }

  def where[RR](condition: T => QueryCondition): UpdateWhere[T, R] = {
    new UpdateWhere[T, R](table, qb.where(condition(table).clause))
  }

  /**
   * Sets a timestamp expiry for the inserted column.
   * This value is set in seconds.
   * @param expiry The expiry time.
   * @return
   */
  def ttl(expiry: Int): UpdateQuery[T, R] = {
    qb.using(QueryBuilder.ttl(expiry))
    this
  }

  def modify(a: T => Assignment): AssignmentsQuery[T, R] = {
    new AssignmentsQuery[T, R](table, qb.`with`(a(table)))
  }
}

class UpdateWhere[T <: CassandraTable[T, R], R](table: T, val qb: Update.Where) extends CQLQuery[UpdateWhere[T, R]] {

  def and[RR](condition: T => QueryCondition): UpdateWhere[T, R] = {
    new UpdateWhere[T, R](table, qb.and(condition(table).clause))
  }

  def onlyIf[RR](condition: T => SecondaryQueryCondition): ConditionalUpdateQuery[T, R] = {
    new ConditionalUpdateQuery[T, R](table, qb.onlyIf(condition(table).clause))
  }

  def modify(a: T => Assignment): AssignmentsQuery[T, R] = {
    new AssignmentsQuery[T, R](table, qb.`with`(a(table)))
  }
}

