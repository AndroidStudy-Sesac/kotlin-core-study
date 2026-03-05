package classobject

fun main() {
    // 1. sealed class
    handleState(UiState.Loading)
    handleState(UiState.Success("Data Loaded"))
    handleState(UiState.Error(Exception("Network Error")))
}

sealed class UiState {
    object Loading : UiState()
    data class Success(val data: String) : UiState()
    data class Error(val exception: Throwable) : UiState()
}

fun handleState(state: UiState) {
    when (state) {
        is UiState.Loading -> println("Loading...")
        is UiState.Success -> println("Success: ${state.data}")
        is UiState.Error -> println("Error: ${state.exception.message}")
    }
}
