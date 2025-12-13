import kotlinx.coroutines.flow.MutableStateFlow

suspend fun <S> MutableStateFlow<S>.withLoadingState(
    applyLoading: (S, Boolean) -> S,
    block: suspend () -> Unit
) {
  value = applyLoading(value, true)

  try {
    block()
  } catch (e: Exception) {
    throw e
  } finally {
    value = applyLoading(value, false)
  }
}
