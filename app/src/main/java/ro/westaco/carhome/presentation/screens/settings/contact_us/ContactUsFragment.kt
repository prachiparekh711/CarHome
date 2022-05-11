package ro.westaco.carhome.presentation.screens.settings.contact_us

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_contact_us.*
import ro.westaco.carhome.R
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.utils.FileUtil
import java.io.File

//C- Contact Us
@AndroidEntryPoint
class ContactUsFragment : BaseFragment<ContactViewModel>() {


    override fun getContentView() = R.layout.fragment_contact_us
    override fun getStatusBarColor() = ContextCompat.getColor(requireContext(), R.color.white)
    var attachmentList: ArrayList<File> = ArrayList()


    override fun initUi() {

        back.setOnClickListener {
            viewModel.onBack()
        }

        home.setOnClickListener {
            viewModel.onMain()
        }

        attachmentList.clear()
    }

    override fun setObservers() {
        viewModel.reasonLiveData?.observe(viewLifecycleOwner) { reasonList ->

            ArrayAdapter(requireContext(), R.layout.spinner_item, reasonList).also { adapter ->
                reasonSpinner.adapter = adapter
            }

            reasonSpinner.setSelection(0)

            cta.setOnClickListener {
                val msg = message.text.toString()
                if (msg.length > 9) {
                    if (msg.length < 5000) {
                        reasonList[reasonSpinner.selectedItemPosition].id?.let { it1 ->
                            viewModel.onSubmit(
                                it1, message.text.toString(), attachmentList
                            )
                        }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            requireContext().resources.getString(R.string.msg_max_msg),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        requireContext().resources.getString(R.string.msg_min_msg),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            attachment.setOnClickListener {
                val result = FileUtil.checkPermission(requireContext())
                if (result) {
                    if (attachmentList.size < 5)
                        openGallery()
                    else {
                        Toast.makeText(
                            requireContext(),
                            requireContext().resources.getString(R.string.attachment_max_msg),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    FileUtil.requestPermission(requireActivity())
                }
            }
        }
    }


    fun openGallery() {
        val imageUri: Uri? = null
        val intent = Intent()
        intent.action = Intent.ACTION_PICK
        intent.type = "image/png"
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, imageUri)
        startActivityForResult(intent, 111)
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            200 -> if (grantResults.size > 0) {
                val READ_EXTERNAL_STORAGE = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val WRITE_EXTERNAL_STORAGE = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (READ_EXTERNAL_STORAGE && WRITE_EXTERNAL_STORAGE) {
                    // perform action when allow permission success
                } else {
                    Toast.makeText(
                        requireContext(),
                        requireContext().resources.getString(R.string.allow_permission),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 111 && resultCode == Activity.RESULT_OK) {
            if (data != null && data.data != null) {

                val selectedUri: Uri? = data.data
                var selectedFile: String? = null
                if (selectedUri != null) {
                    selectedFile = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        FileUtil.getFilePathFor11(requireContext(), selectedUri)
                    } else {
                        FileUtil.getPath(selectedUri, requireContext())
                    }
                }
                if (selectedFile != null) {
                    val mFile = File(selectedFile)
                    if (isFileLessThan10MB(mFile)) {
                        attachmentList.add(mFile)
                        attachmentList.let { attachment.text = it.joinToString(", ") }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            requireContext().resources.getString(R.string.size_msg),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
            }
        }

        if (requestCode == 200 && resultCode == Activity.RESULT_OK) {
            openGallery()
        }
    }

    private fun isFileLessThan10MB(file: File): Boolean {
        val maxFileSize = 10 * 1024 * 1024
        val l = file.length()
        val fileSize = l.toString()
        val finalFileSize = fileSize.toInt()
        return finalFileSize >= maxFileSize
    }
}