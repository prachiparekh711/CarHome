package ro.westaco.carhome.presentation.screens.settings.contact_us

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_contact_us.*
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.Categories
import ro.westaco.carhome.dialog.DialogUtils.Companion.showErrorInfo
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.utils.FileUtil
import ro.westaco.carhome.views.Progressbar
import java.io.File


//C- Contact Us
@AndroidEntryPoint
class ContactUsFragment : BaseFragment<ContactViewModel>() {

    override fun getContentView() = R.layout.fragment_contact_us
    override fun getStatusBarColor() = ContextCompat.getColor(requireContext(), R.color.white)
    var attachmentList: ArrayList<File> = ArrayList()
    var categories: ArrayList<Categories> = ArrayList()
    var categoriesNew: ArrayList<Categories> = ArrayList()
    var progressbar: Progressbar? = null

    override fun initUi() {
        progressbar = Progressbar(requireContext())

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

            for (i in reasonList.indices) {

                if (reasonList[i].parentId == null) {

                    categories.add(reasonList[i])

                    ArrayAdapter(
                        requireContext(),
                        R.layout.spinner_item,
                        categories
                    ).also { adapter ->
                        reasonSpinner.adapter = adapter
                    }
                }
            }

            for (i in reasonList.indices) {

                if (reasonList[i].parentId != null) {


                    categoriesNew.add(reasonList[i])

                    ArrayAdapter(
                        requireContext(),
                        R.layout.spinner_item,
                        categoriesNew
                    ).also { adapter ->
                        subreasonSpinner.adapter = adapter
                    }
                }


            }

            reasonSpinner.setSelection(1)
            subreasonSpinner.setSelection(3)

            cta.setOnClickListener {
                val msg = message.text.toString()

                if (msg.length > 9) {
                    if (msg.length < 5000) {
                        progressbar?.showPopup()
                        reasonList[reasonSpinner.selectedItemPosition].id?.let { it1 ->
                            viewModel.onSubmit(
                                it1, message.text.toString(), attachmentList
                            )
                        }
                    } else {
                        showErrorInfo(requireContext(), getString(R.string.msg_max_msg))
                    }
                } else {
                    showErrorInfo(requireContext(), getString(R.string.msg_min_msg))
                }
            }

            attachment.setOnClickListener {

                if (ActivityCompat.checkSelfPermission(
                        requireActivity(),
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(
                        requireActivity(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {

                    uploadPermission()

                } else {
                    if (permissionUpload()) {
                        openGallery()
                    }
                }

            }
        }

        viewModel.actionStream.observe(viewLifecycleOwner) {
            when (it) {
                is ContactViewModel.ACTION.OnSubmit -> {
                    progressbar?.dismissPopup()
                    MaterialAlertDialogBuilder(
                        requireContext(),
                        R.style.ThemeOverlay_App_MaterialAlertDialog
                    )
                        .setTitle(requireContext().getString(R.string.information_items_))
                        .setCancelable(false)
                        .setMessage(requireContext().getString(R.string.submit_success_msg))
                        .setPositiveButton(
                            "Ok"
                        ) { _, i ->
                            viewModel.onMain()
                        }
                        .show()
                }
            }
        }
    }


    private fun openGallery() {

        if (attachmentList.size < 5) {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/png"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            startActivityForResult(
                Intent.createChooser(intent, requireContext().getString(R.string.select_picture)),
                111
            )
        } else {
            showErrorInfo(
                requireContext(),
                requireContext().getString(R.string.cu_file_info)
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == 111) {

            if (data?.clipData != null) {
                val count = data.clipData?.itemCount

                if (count != null) {
                    var errorInfo: String? = null
                    for (i in 0 until count) {
                        val selectedUri: Uri? = data.clipData?.getItemAt(i)?.uri
                        try {
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
                                    if (!attachmentList.contains(mFile) && attachmentList.size < 5)
                                        attachmentList.add(mFile)
                                } else {
                                    errorInfo =
                                        "${mFile.name} : ${requireContext().getString(R.string.size_msg)}\n"
                                }
                            }

                        } catch (e: Exception) {
                        }
                    }

                    displayText()
                    if (!errorInfo.isNullOrEmpty()) {
                        showErrorInfo(requireContext(), errorInfo)
                    }
                }

            } else if (data?.data != null) {
                val selectedUri: Uri? = data.data
                try {
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
                            if (!attachmentList.contains(mFile) && attachmentList.size < 5)
                                attachmentList.add(mFile)
                            displayText()
                        } else {
                            showErrorInfo(requireContext(), getString(R.string.size_msg))
                        }
                    }

                } catch (e: Exception) {
                }
            }
        }

    }

    private fun displayText() {
        var str1 = ""
        for (j in attachmentList.indices) {
            str1 = if (str1.isEmpty())
                "${requireContext().resources.getString(R.string.img_)}${j + 1} "
            else
                "$str1 ,${requireContext().resources.getString(R.string.img_)}${j + 1}"
        }
        attachment.text = str1
    }

    private fun isFileLessThan10MB(file: File): Boolean {
        val maxFileSize = 10 * 1024 * 1024
        val l = file.length()
        val fileSize = l.toString()
        val finalFileSize = fileSize.toInt()
        return finalFileSize <= maxFileSize
    }

    private fun uploadPermission() {

        Dexter.withContext(requireActivity())
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    report?.let {
                        if (report.areAllPermissionsGranted()) {
                            openGallery()
                        }
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?,
                ) {
                    token?.continuePermissionRequest()
                }
            }).withErrorListener {}

            .check()

    }

    private fun permissionUpload(): Boolean {

        return ActivityCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED

    }

}