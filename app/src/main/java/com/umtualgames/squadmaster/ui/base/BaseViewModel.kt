import androidx.lifecycle.ViewModel
import com.umtualgames.squadmaster.network.RemoteDataSource
import io.reactivex.disposables.CompositeDisposable

abstract class BaseViewModel : ViewModel() {

    val remoteDataSource = RemoteDataSource()

    val compositeDisposable = CompositeDisposable()

    private val errorList: MutableList<Int> = mutableListOf()

    fun clearErrorList() = errorList.clear()

    fun addError(error: Int) = errorList.add(error)

    fun getErrorList() = errorList

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }
}