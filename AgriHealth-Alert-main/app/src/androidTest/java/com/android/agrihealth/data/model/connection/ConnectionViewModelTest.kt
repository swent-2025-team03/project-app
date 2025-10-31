package com.android.agrihealth.data.model.connection

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class ConnectionViewModelTest {

  @get:Rule val mainDispatcherRule = MainDispatcherRule()

  @Test
  fun generateCode_success_setsCodeGenerated() = runTest {
    val repo = mockk<ConnectionRepository>()
    coEvery { repo.generateCode("vet") } returns Result.success("123456")

    val vm = ConnectionViewModel(repo)
    vm.generateCode("vet")
    advanceUntilIdle()

    assertEquals(ConnectionUiState.CodeGenerated("123456"), vm.state.value)
  }

  @Test
  fun generateCode_failure_setsError() = runTest {
    val repo = mockk<ConnectionRepository>()
    coEvery { repo.generateCode(any()) } returns Result.failure(IllegalStateException("boom"))

    val vm = ConnectionViewModel(repo)
    vm.generateCode("vet")
    advanceUntilIdle()

    val s = vm.state.value
    assertTrue(s is ConnectionUiState.Error)
    assertTrue((s as ConnectionUiState.Error).message.contains("boom"))
  }

  @Test
  fun claimCode_success_setsConnected() = runTest {
    val repo = mockk<ConnectionRepository>()
    coEvery { repo.claimCode("111111", "farmer") } returns Result.success("vet123")

    val vm = ConnectionViewModel(repo)
    vm.claimCode("111111", "farmer")
    advanceUntilIdle()

    assertEquals(ConnectionUiState.Connected("vet123"), vm.state.value)
  }

  @Test
  fun claimCode_failure_setsError() = runTest {
    val repo = mockk<ConnectionRepository>()
    coEvery { repo.claimCode(any(), any()) } returns Result.failure(IllegalStateException("used"))

    val vm = ConnectionViewModel(repo)
    vm.claimCode("111111", "farmer")
    advanceUntilIdle()

    val s = vm.state.value
    assertTrue(s is ConnectionUiState.Error)
    assertTrue((s as ConnectionUiState.Error).message.contains("used"))
  }

  @Test
  fun generateCode_cancelsPreviousJob_keepsLatestResult() = runTest {
    val repo = mockk<ConnectionRepository>()

    coEvery { repo.generateCode(any()) } coAnswers
        {
          delay(200) // first coroutine is suspended
          Result.success("OLD")
        } andThenAnswer
        {
          Result.success("NEW")
        }

    val vm = ConnectionViewModel(repo)

    // Start the first coroutine
    vm.generateCode("vet")
    advanceTimeBy(50) // it starts but does not finish

    // Start the second (cancels the first)
    vm.generateCode("vet")
    advanceUntilIdle()

    assertEquals(ConnectionUiState.CodeGenerated("NEW"), vm.state.value)
  }

  @Test
  fun resetState_setsIdle() = runTest {
    val repo = mockk<ConnectionRepository>()
    coEvery { repo.generateCode(any()) } returns Result.success("123456")

    val vm = ConnectionViewModel(repo)
    vm.generateCode("vet")
    advanceUntilIdle()
    assertTrue(vm.state.value is ConnectionUiState.CodeGenerated)

    vm.resetState()
    assertTrue(vm.state.value is ConnectionUiState.Idle)
  }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(private val dispatcher: TestDispatcher = StandardTestDispatcher()) :
    TestWatcher() {
  override fun starting(description: Description?) {
    Dispatchers.setMain(dispatcher)
  }

  override fun finished(description: Description?) {
    Dispatchers.resetMain()
  }
}
