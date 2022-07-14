package ro.westaco.carhome.presentation.screens.documents

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.KeyEvent
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_documents.*
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.Categories
import ro.westaco.carhome.data.sources.remote.responses.models.RowsItem
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.screens.documents.CopyMoveFragment.Companion.actionParent
import ro.westaco.carhome.presentation.screens.documents.CopyMoveFragment.Companion.actionParentIDList
import ro.westaco.carhome.presentation.screens.documents.CopyMoveFragment.Companion.actionPathList
import ro.westaco.carhome.presentation.screens.pdf_viewer.PdfActivity
import ro.westaco.carhome.utils.FileUtil
import ro.westaco.carhome.views.Progressbar
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


@AndroidEntryPoint
class DocumentsFragment : BaseFragment<DocumentsViewModel>(),
    CategoryAdapter.OnItemInteractionListener,
    DocumentAdapter.OnItemInteractionListener,
    DocumentAdapter.OnOptionListener {

    override fun getContentView() = R.layout.fragment_documents
    var progressbar: Progressbar? = null
    lateinit var adapter: CategoryAdapter
    lateinit var docAdapter: DocumentAdapter
    private var docPath: String? = null
    var docPathList: ArrayList<String?> = ArrayList()
    var firstParent = true
    private var parentIDList: ArrayList<Int> = ArrayList()
    private var dialogCreateCategory: BottomSheetDialog? = null
    private var dialogRenameFile: BottomSheetDialog? = null
    private var dialogDeleteFile: BottomSheetDialog? = null
    private var dialogInfoDocument: Dialog? = null
    var dialogNewDocument: BottomSheetDialog? = null
    var dialogOptionDocument: BottomSheetDialog? = null
    private val CAMERA_RESULT = 100
    private val GALLERY_RESULT = 101
    private val DOCUMENT_RESULT = 102
    var selectedFileList: ArrayList<File> = ArrayList()
    var isImage: Boolean = false
    private var newfragment: NewDocumentFragment? = null
    private var actionFragment: CopyMoveFragment? = null
    var categoryMainList: ArrayList<Categories> = ArrayList()
    var documentMainList: ArrayList<RowsItem> = ArrayList()
    var operation: String? = null
    var index = 1
    var catName: EditText? = null

    companion object {
        var selectedList: ArrayList<Int> = ArrayList()
        var selectedDocList: ArrayList<Int> = ArrayList()
        var selectedCatList: ArrayList<Int> = ArrayList()
        var multipleSelection = false
    }

    interface UIChangeListener {
        fun onLongClick()
    }

    override fun onResume() {
        super.onResume()
        view?.isFocusableInTouchMode = true
        view?.requestFocus()
        progressbar?.dismissPopup()
    }

    override fun getStatusBarColor() = ContextCompat.getColor(requireContext(), R.color.white)


    @SuppressLint("NotifyDataSetChanged")
    override fun initUi() {
        progressbar = Progressbar(requireContext())

        createFolderDialog()
        createDocumentDialog()

        addCategory.setOnClickListener {
            catName?.setText("")
            dialogCreateCategory?.show()
        }

        addDocument.setOnClickListener {
            dialogNewDocument?.show()
        }

        requireView().isFocusableInTouchMode = true
        requireView().requestFocus()
        requireView().setOnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {

                if (!multipleSelection)
                    onBackPress()
                else {
                    normalMode()
                    adapter.notifyDataSetChanged()
                    docAdapter.notifyDataSetChanged()
                }
                true
            } else false
        }

        back.setOnClickListener {
            if (!multipleSelection)
                onBackPress()
        }

        home.setOnClickListener {
            viewModel.onMain()
        }

        docPath = resources.getString(R.string.document)
        docPathList.add(docPath)

        val longClickInterface = object : UIChangeListener {
            override fun onLongClick() {
                if (selectedList.size == 0) {
                    normalMode()
                } else {
                    normalState.isVisible = false
                    editState.isVisible = true
                    bottomOptionLL.isVisible = true
                    if (selectedList.size > 1) {
                        bottomInfo.isVisible = false
                        bottomRename.isVisible = false
                    } else {
                        bottomInfo.isVisible = true
                        bottomRename.isVisible = true
                    }
                    addDocument.isVisible = false
                    addCategory.isVisible = false

                    totalSelected.text = requireContext().resources.getString(
                        R.string.doc_selected,
                        selectedList.size, categoryMainList.size + documentMainList.size
                    )
                }
            }
        }

        mRecycler.layoutManager = LinearLayoutManager(context)
        adapter = CategoryAdapter(requireContext(), arrayListOf(), this, longClickInterface)
        mRecycler.adapter = adapter

        mDocRecycler.layoutManager = LinearLayoutManager(context)
        docAdapter =
            DocumentAdapter(requireContext(), arrayListOf(), this, this, longClickInterface)
        mDocRecycler.adapter = docAdapter

        close.setOnClickListener {
            normalMode()
            adapter.notifyDataSetChanged()
            docAdapter.notifyDataSetChanged()
        }

        bottomInfo.setOnClickListener {

            operation = resources.getString(R.string.info)

            if (selectedCatList.size > 0)
                viewModel.getCategoryDetail(selectedList[0])
            else
                viewModel.getDocumentDetail(selectedList[0])
            normalMode()
            adapter.notifyDataSetChanged()
            docAdapter.notifyDataSetChanged()
        }

        bottomRename.setOnClickListener {
            operation = resources.getString(R.string.rename)
            if (selectedCatList.size > 0)
                viewModel.getCategoryDetail(selectedList[0])
            else
                viewModel.getDocumentDetail(selectedList[0])
            normalMode()
            adapter.notifyDataSetChanged()
            docAdapter.notifyDataSetChanged()
        }

        delete.setOnClickListener {
            if (selectedList.size > 1) {
                dialogMultipleDeleteFile(selectedDocList, selectedCatList)
            } else {
                val idToDelete = selectedList[0]
                if (selectedDocList.contains(idToDelete))
                    dialogDeleteFile(selectedDocList[selectedDocList.indexOf(idToDelete)], true)
                else if (selectedCatList.contains(idToDelete))
                    dialogDeleteFile(selectedCatList[selectedCatList.indexOf(idToDelete)], false)
            }
        }

        bottomMove.setOnClickListener {
            moveFunction()
        }

        bottomCopy.setOnClickListener {
            copyFunction()
        }
    }

    fun copyFunction() {
        if (selectedList.size > 0) {
            newDocumentFL.isVisible = true
            actionFragment = CopyMoveFragment()
            val bundle = Bundle()
            bundle.putParcelableArrayList(
                CopyMoveFragment.ARG_CAT_LIST,
                selectedCatList as ArrayList<Parcelable>
            )
            bundle.putParcelableArrayList(
                CopyMoveFragment.ARG_DOC_LIST,
                selectedDocList as ArrayList<Parcelable>
            )
            bundle.putString(
                CopyMoveFragment.ARG_ACTION,
                resources.getString(R.string.copy_here)
            )
            actionFragment?.arguments = bundle
            actionFragment?.let {
                childFragmentManager.beginTransaction()
                    .replace(R.id.newDocumentFL, it)
                    .commit()
            }
        }
    }

    fun moveFunction() {
        if (selectedList.size > 0) {
            newDocumentFL.isVisible = true
            actionFragment = CopyMoveFragment()
            val bundle = Bundle()
            bundle.putParcelableArrayList(
                CopyMoveFragment.ARG_CAT_LIST,
                selectedCatList as ArrayList<Parcelable>
            )
            bundle.putParcelableArrayList(
                CopyMoveFragment.ARG_DOC_LIST,
                selectedDocList as ArrayList<Parcelable>
            )
            bundle.putString(
                CopyMoveFragment.ARG_ACTION,
                resources.getString(R.string.move_here)
            )
            actionFragment?.arguments = bundle
            actionFragment?.let {
                childFragmentManager.beginTransaction()
                    .replace(R.id.newDocumentFL, it)
                    .commit()
            }
        }
    }

    override fun setObservers() {
        viewModel.categoriesLiveData.observe(viewLifecycleOwner) { categoryList ->
            categoryMainList.clear()
            if (categoryList.isNullOrEmpty()) {
                catLL.isVisible = false
            } else {
                categoryMainList = categoryList
                catLL.isVisible = true
                adapter.setItems(categoryList)
                if (firstParent)
                    mPath.isVisible = false
            }
            setEmptyState()
            normalMode()
            progressbar?.dismissPopup()
        }

        viewModel.documentsLiveData.observe(viewLifecycleOwner) { documents ->
            documentMainList.clear()
            if (documents != null) {
                if (documents.rows.isNullOrEmpty()) {
                    docLL.isVisible = false
                } else {
                    documentMainList = documents.rows as ArrayList<RowsItem>
                    docLL.isVisible = true

                    docAdapter.setItems(documents.rows)
                }
            }
            setEmptyState()
            normalMode()
            progressbar?.dismissPopup()
        }

        viewModel.documentsDetailData.observe(viewLifecycleOwner) { rowItem ->
            if (rowItem != null) {
                when (operation) {
                    resources.getString(R.string.info) -> {
                        rowItem.name?.let {
                            rowItem.uploadedDate?.let { it1 ->
                                rowItem.fileSize?.let { it2 ->
                                    dialogInfoDocument(
                                        it,
                                        it1,
                                        it2
                                    )
                                    normalMode()
                                }
                            }
                        }
                    }

                    resources.getString(R.string.rename) -> {
                        rowItem.id?.let { it1 ->
                            rowItem.name?.let { it2 ->
                                dialogRenameFile(
                                    it1,
                                    it2,
                                    true
                                )
                            }
                        }
                    }
                }
            }
        }

        viewModel.categoryDetailData.observe(viewLifecycleOwner) { categoryItem ->
            if (categoryItem != null) {
                when (operation) {
                    resources.getString(R.string.info) -> {
                        categoryItem.name.let {
                            categoryItem.createdDate?.let { it1 ->
                                categoryItem.size?.let { it2 ->
                                    dialogInfoDocument(
                                        it,
                                        it1,
                                        it2
                                    )

                                }
                            }
                        }
                    }
                    resources.getString(R.string.rename) -> {
                        categoryItem.id?.let { it1 ->
                            dialogRenameFile(
                                it1,
                                categoryItem.name,
                                false
                            )
                        }
                    }
                }
            }
        }
    }

    private fun setEmptyState() {
        emptyStateGroup.isVisible = categoryMainList.isEmpty() && documentMainList.isEmpty()
    }

    override fun onItemClick(item: Categories) {

        adapter.clearAll()
        docAdapter.clearAll()
        if (firstParent) {
            firstParent = false
            mPath.isVisible = true
        }
        item.id?.let { parentIDList.add(it) }

        addCategory.isVisible = false
        addDocument.isVisible = true
        progressbar?.showPopup()
        viewModel.fetchCategories(item.id)
        item.id?.let { viewModel.fetchDocuments(it) }

        docPathList.add(item.name)

        setPath(docPathList)
    }

    override fun onItemClick(item: RowsItem) {
        val intent = Intent(requireContext(), PdfActivity::class.java)
        intent.putExtra(PdfActivity.ARG_FROM, "DOCUMENT")
        intent.putExtra(PdfActivity.ARG_ITEM, item)
        requireContext().startActivity(intent)
    }


    private fun setPath(docPathList: ArrayList<String?>) {

        docPath = ""
        if (docPathList.size > 3) {
            docPath += docPathList[0] + docPathList[1] + " > " + " ... " + " > " + docPathList[docPathList.size - 1]

        } else {
            for (i in docPathList.indices) {
                docPath += if (i == 0) {
                    docPathList[i]
                } else {
                    " > " + docPathList[i]
                }
            }
        }

        val switchDescriptionSpannable = SpannableString(docPath)
        val switchCtaStr = docPathList[docPathList.size - 1]
        val switchCtaStart = switchCtaStr?.let { switchDescriptionSpannable.indexOf(it) }
        if (switchCtaStart != null) {
            switchDescriptionSpannable.setSpan(
                ForegroundColorSpan(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.clickable_subtext
                    )
                ),
                switchCtaStart,
                switchCtaStart + switchCtaStr.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        mPath.text = switchDescriptionSpannable
    }

    fun normalMode() {
        multipleSelection = false
        selectedList.clear()
        selectedDocList.clear()
        selectedCatList.clear()

        normalState.isVisible = true
        editState.isVisible = false
        bottomOptionLL.isVisible = false
        if (parentIDList.size == 0) {
            addDocument.isVisible = false
            addCategory.isVisible = true
        } else {
            addDocument.isVisible = true
            addCategory.isVisible = false
        }
    }

    fun onBackPress() {
        documentMainList.clear()
        categoryMainList.clear()
        selectedList.clear()
        selectedDocList.clear()
        if (newDocumentFL.isVisible) {
            newDocumentFL.isVisible = false
            selectedFileList.clear()
            var id: Int? = null
            newfragment?.let {
                childFragmentManager.beginTransaction().remove(it).commit()
                id = parentIDList[parentIDList.size - 1]
            }
            actionFragment?.let {
                childFragmentManager.beginTransaction().remove(it).commit()
                id = actionParent
                if (actionParent != null) {
                    parentIDList.clear()
                    parentIDList.addAll(actionParentIDList)
                    docPathList.clear()
                    docPathList.addAll(actionPathList)
                    firstParent = CopyMoveFragment.actionFirstParent
                    mPath.isVisible = true
                    setPath(docPathList)
                }
            }

            viewModel.fetchCategories(id)
            id.let {
                if (it != null) {
                    viewModel.fetchDocuments(it)
                }
            }
        } else {
            if (firstParent) {
                viewModel.onBack()
            } else {
                progressbar?.showPopup()
                if (parentIDList.size > 1) {
                    viewModel.fetchCategories(parentIDList[parentIDList.size - 2])
                    parentIDList[parentIDList.size - 2].let {
                        viewModel.fetchDocuments(
                            it
                        )
                    }
                    parentIDList.removeAt(parentIDList.size - 1)
                    docPathList.removeAt(docPathList.size - 1)
                    setPath(docPathList)
                    addDocument.isVisible = true
                    addCategory.isVisible = false
                } else if (parentIDList.size == 1) {
                    viewModel.fetchCategories(null)
                    docLL.isVisible = false
                    firstParent = true
                    docPathList.clear()
                    parentIDList.clear()
                    docPath = "${resources.getString(R.string.document)}"
                    docPathList.add(docPath)
                    mPath.text = docPathList[0]
                    addDocument.isVisible = false
                    addCategory.isVisible = true
                }
            }
        }
    }

    private fun createFolderDialog() {
        val view = layoutInflater.inflate(R.layout.create_folder_dialog, null)
        dialogCreateCategory = BottomSheetDialog(requireContext())
        dialogCreateCategory?.setCancelable(true)
        dialogCreateCategory?.setContentView(view)
        val title = view.findViewById<TextView>(R.id.title)
        catName = view.findViewById(R.id.catName)
        val cancel = view.findViewById<ImageView>(R.id.cancel)
        val done = view.findViewById<ImageView>(R.id.done)

        title.text = resources.getString(R.string.create_new_category)
        catName?.setText("")

        cancel.setOnClickListener {
            catName?.setText("")
        }

        done.setOnClickListener {
            val result = checkNewNameExist(catName?.text.toString())
            if (result) {
                val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
                val dialog: AlertDialog = builder.setTitle(resources.getString(R.string.app_name))
                    .setMessage(resources.getString(R.string.folder_exist))
                    .setPositiveButton(resources.getString(R.string.yes)) { dialog, which ->
                        index = 1
                        changeName(
                            catName?.text.toString(),
                            catName?.text.toString(),
                            false,
                            true,
                            null
                        )

                        progressbar?.showPopup()
                        dialog.dismiss()
                    }
                    .setNegativeButton(resources.getString(R.string.no)) { dialog, which ->
                        dialog.dismiss()
                    }
                    .create()
                dialog.setCancelable(false)
                dialog.show()
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE)
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.DKGRAY)
            } else {
                progressbar?.showPopup()
                if (parentIDList.isNotEmpty())
                    viewModel.addCategory(
                        parentIDList[parentIDList.size - 1],
                        catName?.text.toString()
                    )
                else
                    viewModel.addCategory(null, catName?.text.toString())
            }
            dialogCreateCategory?.dismiss()
        }
    }

    private fun checkNewNameExist(newName: String): Boolean {
        var result = false
        for (i in categoryMainList.indices) {
            if (categoryMainList[i].name == newName) {
                result = true
                break
            }
        }
        return result
    }

    private fun changeName(
        originalName: String?,
        newName: String,
        recursiveCall: Boolean,
        isCreate: Boolean,
        parentIDForRename: Int?
    ) {
        var orgName = originalName
        var categoryName = newName

        if (recursiveCall) {
            val indexString =
                categoryName.substring(categoryName.indexOf("(") + 1, categoryName.indexOf(")"))
            val i = indexString.toInt().plus(1)
            categoryName = "$orgName ($i)"
        } else {
            orgName = categoryName
            categoryName = "$categoryName ($index)"
        }
        val result = checkNewNameExist(categoryName)
        progressbar?.showPopup()
        if (result) {
            changeName(originalName, categoryName, true, isCreate, parentIDForRename)
        } else {
            if (isCreate) {

                if (parentIDList.isNotEmpty())
                    viewModel.addCategory(
                        parentIDList[parentIDList.size - 1],
                        categoryName
                    )
                else
                    viewModel.addCategory(null, categoryName)
            } else {
                if (parentIDList.size == 0) {
                    if (parentIDForRename != null) {
                        viewModel.editCategory(
                            null,
                            categoryName,
                            parentIDForRename
                        )
                    }
                } else {
                    if (parentIDForRename != null) {
                        viewModel.editCategory(
                            parentIDList[parentIDList.size - 1],
                            categoryName,
                            parentIDForRename
                        )
                    }
                }
            }
        }
    }


    private fun dialogRenameFile(id: Int, name: String, isFile: Boolean) {
        val view = layoutInflater.inflate(R.layout.create_folder_dialog, null)
        dialogRenameFile = BottomSheetDialog(requireContext())
        dialogRenameFile?.setCancelable(true)
        dialogRenameFile?.setContentView(view)
        val title = view.findViewById<TextView>(R.id.title)
        val catName = view.findViewById<EditText>(R.id.catName)
        val cancel = view.findViewById<ImageView>(R.id.cancel)
        val done = view.findViewById<ImageView>(R.id.done)

        if (isFile)
            title.text = resources.getString(R.string.rename_file)
        else
            title.text = resources.getString(R.string.rename_cat)

        catName.setText(name)

        cancel.setOnClickListener {
            catName.setText("")
        }

        done.setOnClickListener {
            if (isFile)
                viewModel.editDocument(
                    parentIDList[parentIDList.size - 1],
                    catName.text.toString(),
                    id
                )
            else {
                val result = checkNewNameExist(catName?.text.toString())
                if (result) {
                    val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
                    val dialog: AlertDialog =
                        builder.setTitle(resources.getString(R.string.app_name))
                            .setMessage(resources.getString(R.string.folder_exist))
                            .setPositiveButton(resources.getString(R.string.yes)) { dialog, which ->
                                index = 1
                                changeName(
                                    catName?.text.toString(),
                                    catName?.text.toString(),
                                    false,
                                    false,
                                    id
                                )

                                progressbar?.showPopup()
                                dialog.dismiss()
                            }
                            .setNegativeButton(resources.getString(R.string.no)) { dialog, which ->
                                dialog.dismiss()
                            }
                            .create()
                    dialog.setCancelable(false)
                    dialog.show()
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE)
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.DKGRAY)
                } else {
                    progressbar?.showPopup()
                    if (parentIDList.size == 0) {
                        viewModel.editCategory(
                            null,
                            catName.text.toString(),
                            id
                        )
                    } else {
                        viewModel.editCategory(
                            parentIDList[parentIDList.size - 1],
                            catName.text.toString(),
                            id
                        )
                    }
                }

                /* if (parentIDList.size == 0) {
                     viewModel.editCategory(
                         null,
                         catName.text.toString(),
                         id
                     )
                 } else {
                     viewModel.editCategory(
                         parentIDList[parentIDList.size - 1],
                         catName.text.toString(),
                         id
                     )
                 }*/
            }
//            progressbar?.showPopup()
            dialogRenameFile?.dismiss()
        }
        dialogRenameFile?.show()
    }

    private fun createOptionDialog(item: RowsItem) {
        val view = layoutInflater.inflate(R.layout.document_option_layout, null)
        dialogOptionDocument = BottomSheetDialog(requireContext())
        dialogOptionDocument?.setCancelable(true)
        dialogOptionDocument?.setContentView(view)
        val mName = view.findViewById<TextView>(R.id.mName)
        val rename = view.findViewById<LinearLayout>(R.id.rename)
        val move = view.findViewById<LinearLayout>(R.id.move)
        val copy = view.findViewById<LinearLayout>(R.id.copy)
        val delete = view.findViewById<LinearLayout>(R.id.delete)
        val info = view.findViewById<RelativeLayout>(R.id.info)
        val close = view.findViewById<ImageView>(R.id.close)

        mName.text = item.name

        close.setOnClickListener {
            dialogOptionDocument?.dismiss()
        }

        rename.setOnClickListener {
            dialogOptionDocument?.dismiss()
            item.id?.let { it1 -> item.name?.let { it2 -> dialogRenameFile(it1, it2, true) } }
        }

        move.setOnClickListener {
            item.id?.let { it1 -> selectedList.add(it1) }
            item.id?.let { it1 -> selectedDocList.add(it1) }
            moveFunction()
            dialogOptionDocument?.dismiss()
        }

        copy.setOnClickListener {
            item.id?.let { it1 -> selectedList.add(it1) }
            item.id?.let { it1 -> selectedDocList.add(it1) }
            copyFunction()
            dialogOptionDocument?.dismiss()
        }

        delete.setOnClickListener {
            dialogOptionDocument?.dismiss()
            item.id?.let { it1 -> dialogDeleteFile(it1, true) }
        }

        info.setOnClickListener {
            dialogOptionDocument?.dismiss()
            item.name?.let { it1 ->
                item.uploadedDate?.let { it2 ->
                    item.fileSize?.let { it3 ->
                        dialogInfoDocument(
                            it1,
                            it2, it3
                        )
                    }
                }
            }
        }

        dialogOptionDocument?.show()
    }

    private fun dialogDeleteFile(id: Int, isFile: Boolean) {
        val view = layoutInflater.inflate(R.layout.dialog_document_delete, null)
        dialogDeleteFile = BottomSheetDialog(requireContext())
        dialogDeleteFile?.setCancelable(true)
        dialogDeleteFile?.setContentView(view)
        val title = view.findViewById<TextView>(R.id.title)
        val subtitle = view.findViewById<TextView>(R.id.subtitle)
        val negative = view.findViewById<TextView>(R.id.neg)
        val positive = view.findViewById<TextView>(R.id.pos)

        title.text = resources.getString(R.string.delete)
        if (isFile)
            subtitle.text = resources.getString(R.string.delete_doc_subtitle)
        else
            subtitle.text = resources.getString(R.string.delete_multiple_subtitle)

        positive.setOnClickListener {
            if (isFile)
                viewModel.deleteDocument(parentIDList[parentIDList.size - 1], id)
            else {
                if (firstParent) {
                    viewModel.deleteCategory(null, id)
                } else {
                    viewModel.deleteCategory(parentIDList[parentIDList.size - 1], id)
                }
            }

            normalMode()
            adapter.notifyDataSetChanged()
            docAdapter.notifyDataSetChanged()
            progressbar?.showPopup()
            dialogDeleteFile?.dismiss()
        }

        negative.setOnClickListener {
            dialogDeleteFile?.dismiss()
        }

        dialogDeleteFile?.show()
    }

    private fun dialogMultipleDeleteFile(docList: ArrayList<Int>, catList: ArrayList<Int>) {

        val view = layoutInflater.inflate(R.layout.dialog_document_delete, null)
        dialogDeleteFile = BottomSheetDialog(requireContext())
        dialogDeleteFile?.setCancelable(true)
        dialogDeleteFile?.setContentView(view)
        val title = view.findViewById<TextView>(R.id.title)
        val subtitle = view.findViewById<TextView>(R.id.subtitle)
        val negative = view.findViewById<TextView>(R.id.neg)
        val positive = view.findViewById<TextView>(R.id.pos)

        title.text =
            "${resources.getString(R.string.delete_multiple, docList.size + catList.size)} "
        subtitle.text = resources.getString(R.string.delete_multiple_subtitle)

        positive.setOnClickListener {

            if (firstParent) {
                viewModel.deleteDocumentandCategory(
                    null,
                    docList,
                    catList
                )
            } else {
                viewModel.deleteDocumentandCategory(
                    parentIDList[parentIDList.size - 1],
                    docList,
                    catList
                )
            }

            progressbar?.showPopup()
            dialogDeleteFile?.dismiss()
        }

        negative.setOnClickListener {
            dialogDeleteFile?.dismiss()
        }

        dialogDeleteFile?.show()
    }

    private fun dialogInfoDocument(name: String, uploadedDate: String, fileSize: Int) {
        val view = layoutInflater.inflate(R.layout.dialog_doc_info, null)
        dialogInfoDocument = Dialog(requireContext())
        dialogInfoDocument?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogInfoDocument?.setCancelable(true)
        dialogInfoDocument?.setContentView(view)
        val close = view.findViewById<ImageView>(R.id.close)
        val nameTV = view.findViewById<TextView>(R.id.nameTV)
        val dateTV = view.findViewById<TextView>(R.id.dateTV)
        val sizeTV = view.findViewById<TextView>(R.id.sizeTV)
        val pathTV = view.findViewById<TextView>(R.id.pathTV)

        nameTV.text = name

        var spf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val newDate: Date = spf.parse(uploadedDate)
        spf = SimpleDateFormat("dd MMMM yyyy hh:mmaa")
        dateTV.text = "${spf.format(newDate) ?: ""}"

        sizeTV.text = fileSize.toLong().let { FileUtil.getSize(it) }
        var path = ""
        for (i in docPathList.indices) {
            path += if (i == 0) {
                docPathList[i]
            } else {
                " / " + docPathList[i]
            }
        }
        pathTV.text = path

        close.setOnClickListener {
            dialogInfoDocument?.dismiss()
        }

        dialogInfoDocument?.show()
    }


    private fun createDocumentDialog() {


        val view = layoutInflater.inflate(R.layout.create_new_document_dialog, null)
        dialogNewDocument = BottomSheetDialog(requireContext())
        dialogNewDocument?.setCancelable(true)
        dialogNewDocument?.setContentView(view)
        val mFolder = view.findViewById<LinearLayout>(R.id.mFolder)
        val mCamera = view.findViewById<LinearLayout>(R.id.mCamera)
        val mPhotos = view.findViewById<LinearLayout>(R.id.mPhotos)
        val mDocument = view.findViewById<LinearLayout>(R.id.mDocument)
        val close = view.findViewById<ImageView>(R.id.close)

        close.setOnClickListener {
            dialogNewDocument?.dismiss()
        }

        mFolder.setOnClickListener {
            catName?.setText("")
            dialogNewDocument?.dismiss()
            dialogCreateCategory?.show()
        }

        mCamera.setOnClickListener {

            if (ActivityCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                onCameraPermission()
            } else {
                if (permission()) {
                    callCameraIntent()
                }
            }

            dialogNewDocument?.dismiss()

        }

        mPhotos.setOnClickListener {

            if (ActivityCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                Dexter.withContext(requireActivity())
                    .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                    .withListener(object : MultiplePermissionsListener {
                        override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                            report?.let {
                                if (report.areAllPermissionsGranted()) {
                                    callPhotosIntent()
                                }
                            }
                        }

                        override fun onPermissionRationaleShouldBeShown(
                            permissions: MutableList<PermissionRequest>?,
                            token: PermissionToken?
                        ) {
                            token?.continuePermissionRequest()
                        }
                    }).withErrorListener {}

                    .check()

            } else {
                if (permissionUpload()) {
                    callPhotosIntent()
                }
            }

            dialogNewDocument?.dismiss()
        }

        mDocument.setOnClickListener {

            if (ActivityCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                Dexter.withContext(requireActivity())
                    .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                    .withListener(object : MultiplePermissionsListener {
                        override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                            report?.let {
                                if (report.areAllPermissionsGranted()) {
                                    callDocumentIntent()
                                }
                            }
                        }

                        override fun onPermissionRationaleShouldBeShown(
                            permissions: MutableList<PermissionRequest>?,
                            token: PermissionToken?
                        ) {
                            token?.continuePermissionRequest()
                        }
                    }).withErrorListener {}

                    .check()

            } else {

                if (permissionUpload()) {
                    callDocumentIntent()
                }
            }

            dialogNewDocument?.dismiss()
        }
    }

    private fun callPhotosIntent() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(
            Intent.createChooser(intent, requireContext().getString(R.string.select_picture)),
            GALLERY_RESULT
        )
    }

    private fun callCameraIntent() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA_RESULT)
    }

    private fun callDocumentIntent() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "application/pdf"
        startActivityForResult(
            Intent.createChooser(intent, requireContext().getString(R.string.select_pdf)),
            DOCUMENT_RESULT
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        selectedFileList.clear()
        if (requestCode == CAMERA_RESULT && resultCode == Activity.RESULT_OK) {
            val photo = data?.extras?.get("data") as Bitmap?
            val imageUri = getImageUri(requireContext(), photo)
            var selectedFile: String? = null
            try {
                selectedFile = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    FileUtil.getFilePathFor11(requireContext(), imageUri)
                } else {
                    FileUtil.getPath(imageUri, requireContext())
                }
                if (selectedFile != null) {
                    isImage = true
                    selectedFileList.add(File(selectedFile))
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
                                selectedFileList.add(File(selectedFile))
                            }

                        } catch (e: Exception) {
                        }
                    }
                    isImage = true
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
                        selectedFileList.add(File(selectedFile))
                        isImage = true
                    }

                } catch (e: Exception) {
                }
            }
        }

        if (resultCode == Activity.RESULT_OK && requestCode == DOCUMENT_RESULT) {
            val selectedUri: Uri? = data?.data
            if (selectedUri != null) {
                try {
                    var selectedFile: String? = null
                    selectedFile = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        FileUtil.getFilePathFor11(requireContext(), selectedUri)
                    } else {
                        FileUtil.getPath(selectedUri, requireContext())
                    }
                    if (selectedFile != null) {
                        selectedFileList.add(File(selectedFile))
                    }

                } catch (e: Exception) {
                }
            }
            isImage = false
        }


        if (selectedFileList.isNotEmpty()) {
            newDocumentFL.isVisible = true
            newfragment = NewDocumentFragment()
            val bundle = Bundle()
            bundle.putParcelableArrayList(
                NewDocumentFragment.ARG_LIST,
                selectedFileList as ArrayList<Parcelable>
            )
            bundle.putBoolean(NewDocumentFragment.ARG_IS_IMG, isImage)
            bundle.putInt(NewDocumentFragment.ARG_CAT_ID, parentIDList[parentIDList.size - 1])
            newfragment?.arguments = bundle
            newfragment?.let {
                childFragmentManager.beginTransaction()
                    .replace(R.id.newDocumentFL, it)
                    .commit()
            }
        }
    }

    private fun getImageUri(inContext: Context, inImage: Bitmap?): Uri {
        val outImage = inImage?.let { Bitmap.createScaledBitmap(it, 1000, 1000, true) }
        val path =
            MediaStore.Images.Media.insertImage(inContext.contentResolver, outImage, "Title", null)
        return Uri.parse(path)
    }

    override fun onOptionClick(item: RowsItem) {
        createOptionDialog(item)
    }

    private fun onCameraPermission() {

        Dexter.withContext(requireContext())
            .withPermission(Manifest.permission.CAMERA)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                    callCameraIntent()
                }

                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {}

                override fun onPermissionRationaleShouldBeShown(
                    p0: PermissionRequest?,
                    p1: PermissionToken?,
                ) {
                    p1?.continuePermissionRequest()
                }
            }).withErrorListener {}

            .check()

    }


    private fun permission(): Boolean {

        return ActivityCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

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