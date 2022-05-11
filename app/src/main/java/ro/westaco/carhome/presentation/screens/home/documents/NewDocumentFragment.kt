package ro.westaco.carhome.presentation.screens.home.documents

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Parcelable
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_new_document.*
import ro.westaco.carhome.R
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.utils.FileUtil
import ro.westaco.carhome.utils.Progressbar
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class NewDocumentFragment : BaseFragment<DocumentsViewModel>(),
    ImageAdapter.OnItemInteractionListener {

    lateinit var imgAdapter: ImageAdapter

    var selectedList: ArrayList<File> = ArrayList()
    var isImage = false
    var catID: Int? = null
    private val CAMERA_RESULT = 100
    private val GALLERY_RESULT = 101
    private val DOCUMENT_RESULT = 102
    var progressbar: Progressbar? = null
    private var dialogSave: BottomSheetDialog? = null

    companion object {
        const val ARG_LIST = "arg_list"
        const val ARG_IS_IMG = "arg_is_img"
        const val ARG_CAT_ID = "arg_cat_id"
    }

    override fun onItemClick(position: Int) {
        selectedList.removeAt(position)
    }

    override fun getContentView() = R.layout.fragment_new_document

    override fun initUi() {
        arguments?.let {
            selectedList = it.getParcelableArrayList<Parcelable>(ARG_LIST) as ArrayList<File>
            isImage = it.getBoolean(ARG_IS_IMG)
            catID = it.getInt(ARG_CAT_ID)
        }

        progressbar = Progressbar(requireContext())
        progressbar?.showPopup()

        setSaveDialog()
        setData()

        mCamera.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, CAMERA_RESULT)
        }

        mGallery.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            startActivityForResult(
                Intent.createChooser(intent, requireContext().getString(R.string.select_picture)),
                GALLERY_RESULT
            )
        }

        mDocument.setOnClickListener {
            selectedList.clear()
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "application/pdf"
            startActivityForResult(
                Intent.createChooser(intent, requireContext().getString(R.string.select_pdf)),
                DOCUMENT_RESULT
            )
        }
    }

    private fun setData() {
        if (isImage) {
            imageRL.isVisible = true
            imageLL.isVisible = true
            docLL.isVisible = false
            docRL.isVisible = false

            mRecycler.layoutManager = GridLayoutManager(context, 3)
            imgAdapter = ImageAdapter(requireContext(), arrayListOf(), this)
            mRecycler.adapter = imgAdapter

            if (selectedList.isNotEmpty())
                imgAdapter.setItems(selectedList)

            val sdf = SimpleDateFormat("yy-MM-dd hh:mm")
            val currentDate = sdf.format(Date())
            fileName.setText("${resources.getString(R.string.untitled)} ${currentDate}")

            save.setOnClickListener {
                dialogSave?.show()
            }
        } else {
            imageRL.isVisible = false
            imageLL.isVisible = false
            docLL.isVisible = true
            docRL.isVisible = true

            mName.text = selectedList[0].name.toString()
            fileName.setText(selectedList[0].nameWithoutExtension)
            mSize.text = FileUtil.getSize(selectedList[0].length())

            fileName.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    mName.text = s
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }
            })

            upload.setOnClickListener {
                progressbar?.showPopup()
                catID?.let { it1 ->
                    viewModel.addDocument(
                        it1,
                        fileName.text.toString(),
                        null,
                        selectedList
                    )
                }
            }
        }
        progressbar?.dismissPopup()
    }

    override fun setObservers() {
        viewModel.actionStream.observe(viewLifecycleOwner) {
            when (it) {
                is DocumentsViewModel.ACTION.onBackofSuccess -> {
                    progressbar?.dismissPopup()
                    var parentFrag: DocumentsFragment? = null
                    parentFrag = this@NewDocumentFragment.parentFragment as DocumentsFragment
                    parentFrag.onBackPress()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CAMERA_RESULT && resultCode == Activity.RESULT_OK) {
            val photo = data?.extras?.get("data") as Bitmap?
            val selectedUri = getImageUri(requireContext(), photo)
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
                    selectedList.add(File(selectedFile))
                }

            } catch (e: Exception) {
            }

        }

        if (resultCode == Activity.RESULT_OK && requestCode == GALLERY_RESULT) {

            if (data?.clipData != null) {
                val count = data.clipData?.itemCount

                if (count != null) {
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
                                selectedList.add(File(selectedFile))
                            }

                        } catch (e: Exception) {
                        }
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
                        selectedList.add(File(selectedFile))
                    }

                } catch (e: Exception) {
                }
            }
        }

        if (resultCode == Activity.RESULT_OK && requestCode == DOCUMENT_RESULT) {
            selectedList.clear()
            val selectedUri: Uri? = data?.data
            if (selectedUri != null) {
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
                        selectedList.add(File(selectedFile))
                    }

                } catch (e: Exception) {
                }
            }
        }

        if (selectedList.isNotEmpty()) {
            setData()
        }
    }

    private fun getImageUri(inContext: Context, inImage: Bitmap?): Uri {
        val outImage = inImage?.let { Bitmap.createScaledBitmap(it, 1000, 1000, true) }
        val path =
            MediaStore.Images.Media.insertImage(inContext.contentResolver, outImage, "Title", null)
        return Uri.parse(path)
    }

    private fun setSaveDialog() {
        val view = layoutInflater.inflate(R.layout.save_doc_img_dialog, null)
        dialogSave = BottomSheetDialog(requireContext())
        dialogSave?.setCancelable(true)
        dialogSave?.setContentView(view)
        val close = view.findViewById<ImageView>(R.id.close)
        val multiple = view.findViewById<LinearLayout>(R.id.multiple)
        val single = view.findViewById<LinearLayout>(R.id.single)

        close.setOnClickListener {
            dialogSave?.dismiss()
        }

        multiple.setOnClickListener {
            dialogSave?.dismiss()

            catID?.let { it1 ->
                viewModel.addDocument(
                    it1,
                    fileName.text.toString(),
                    "IMG",
                    selectedList
                )
            }

        }

        single.setOnClickListener {
            dialogSave?.dismiss()
            catID?.let { it1 ->
                viewModel.addDocument(
                    it1,
                    fileName.text.toString(),
                    "PDF",
                    selectedList
                )
            }
        }
    }
}