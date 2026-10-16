var selectedDate by remember { mutableStateOf(LocalDate.now()) }
var selectedTime by remember { mutableStateOf(LocalTime.now()) }
val dateTimeStr = "\( {selectedDate}T \){selectedTime}"

// Date button
Button(onClick = { datePickerState.show() }) {
    Text(selectedDate.toString())
}

// Time button
Button(onClick = { timePickerState.show() }) {
    Text(selectedTime.toString())
}

// Analysis call
vm.runAnalysis(clientId, token, symbol, timeframe, dateTimeStr)
