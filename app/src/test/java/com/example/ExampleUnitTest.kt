package com.example

import com.example.ui.screens.evaluateExpression
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testCalculatorSimpleAddition() {
    assertEquals(150.0, evaluateExpression("100 + 50"), 0.001)
  }

  @Test
  fun testCalculatorPrecedence() {
    // 100 + 50 * 2 = 200
    assertEquals(200.0, evaluateExpression("100 + 50 × 2"), 0.001)
  }

  @Test
  fun testCalculatorDivision() {
    assertEquals(25.0, evaluateExpression("100 ÷ 4"), 0.001)
  }

  @Test
  fun testCalculatorDecimals() {
    assertEquals(12.75, evaluateExpression("10.5 + 2.25"), 0.001)
  }

  @Test
  fun testCalculatorTrailingOperatorLiveResult() {
    // While typing "150 +", it should evaluate to 150.0
    assertEquals(150.0, evaluateExpression("150 +"), 0.001)
  }

  @Test
  fun testCalculatorEmptyExpression() {
    assertEquals(0.0, evaluateExpression(""), 0.001)
  }
}

