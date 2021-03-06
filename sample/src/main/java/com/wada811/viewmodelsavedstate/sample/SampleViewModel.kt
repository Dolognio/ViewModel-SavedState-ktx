package com.wada811.viewmodelsavedstate.sample

import androidx.lifecycle.*
import com.wada811.viewmodelsavedstate.SavedStateAdapter
import com.wada811.viewmodelsavedstate.liveData
import kotlinx.coroutines.delay

class SampleViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    enum class CountUpValue(val count: Int) {
        ONE(1),
        TEN(10)
    }

    private var viewModelCount = MutableLiveData(0)
    var viewModelCountText: LiveData<String> = MediatorLiveData<String>().also { liveData ->
        liveData.addSource(viewModelCount) { count ->
            liveData.value = "$count"
        }
    }
    val countUpValueEnumLiveData: MutableLiveData<CountUpValue?> by savedStateHandle.liveData(object : SavedStateAdapter<CountUpValue?, Int?> {
        override fun toSavedState(value: CountUpValue?): Int? = value?.ordinal
        override fun fromSavedState(state: Int?): CountUpValue? = CountUpValue.values().firstOrNull { it.ordinal == state }
    }, defaultValue = CountUpValue.ONE)
    val countUpValueEnumSuspendLiveData: MutableLiveData<CountUpValue?> by savedStateHandle.liveData(object :
            SavedStateAdapter<CountUpValue?, Int?> {
        override fun toSavedState(value: CountUpValue?): Int? = value?.ordinal
        override fun fromSavedState(state: Int?): CountUpValue? =
                CountUpValue.values().firstOrNull() { it.ordinal == state }
    },
        defaultValueLoader = { longLoadingCountUp() })
    val savedStateCount: MutableLiveData<Int> by savedStateHandle.liveData(0)
    var savedStateCountText: LiveData<String> = MediatorLiveData<String>().also { liveData ->
        liveData.addSource(savedStateCount) { count ->
            liveData.value = "$count"
        }
    }
    val liveDataInitializedFromSuspendFunction: LiveData<String?> by savedStateHandle.liveData(
        defaultValueLoader = { longLoading() })
    val log: MutableLiveData<String> by savedStateHandle.liveData("Log:")

    init {
        appendLog("ViewModel::init")
    }

    fun countUp() {
        viewModelCount.value = viewModelCount.value?.plus(countUpValueEnumLiveData.value?.count ?: 0)
        savedStateCount.value = savedStateCount.value?.plus(countUpValueEnumLiveData.value?.count ?: 0)
    }

    fun appendLog(text: String) {
        val maxLineCount = 5
        val logLines = log.value!!.split("\n")
        if (logLines.size > maxLineCount) {
            log.value =
                logLines.subList(logLines.size - maxLineCount + 1, logLines.size).joinToString("\n")
        }
        log.value = log.value + "\n$text"
    }

    override fun onCleared() {
        super.onCleared()
        appendLog("ViewModel::onCleared")
    }

    private suspend fun longLoading(): String {
        delay(2000)
        return "String loaded from suspend function."
    }

    private suspend fun longLoadingCountUp(): CountUpValue {
        delay(4000)
        return CountUpValue.ONE
    }
}
