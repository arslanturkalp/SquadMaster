import androidx.fragment.app.Fragment
import com.umtualgames.squadmaster.ui.generic.GenericProgressDialog

open class BaseFragment : Fragment() {

    private var progressDialog: GenericProgressDialog? = null

    fun showProgressDialog() {
        progressDialog = GenericProgressDialog()
        progressDialog?.show(childFragmentManager, "ProgressDialog")
    }

    fun dismissProgressDialog() {
        progressDialog?.dismissAllowingStateLoss()
    }

}