package ro.westaco.carhome.presentation.screens.documents

import android.app.AlertDialog
import android.graphics.Color
import android.os.Parcelable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_copy_move.*
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.Categories
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.utils.Progressbar

@AndroidEntryPoint
class CopyMoveFragment : BaseFragment<DocumentOperationViewModel>(),
    CategoryOperationAdapter.OnItemInteractionListener {
    override fun getContentView() = R.layout.fragment_copy_move

    var selectedDocList: java.util.ArrayList<Int> = java.util.ArrayList()
    var selectedCatList: java.util.ArrayList<Int> = java.util.ArrayList()
    lateinit var adapter: CategoryOperationAdapter
    var progressbar: Progressbar? = null

    var categoryMainList: ArrayList<Categories> = ArrayList()

    private var docPath: String? = null
    var action: String? = null
    private var dialogCreateCategory: BottomSheetDialog? = null
    var index = 1


    companion object {
        const val ARG_CAT_LIST = "arg_cat_list"
        const val ARG_DOC_LIST = "arg_doc_list"
        const val ARG_ACTION = "arg_action"
        var actionParent: Int? = null
        var actionPathList: ArrayList<String?> = ArrayList()
        var actionParentIDList: ArrayList<Int> = ArrayList()
        var actionFirstParent = true
    }

    override fun initUi() {
        actionPathList.clear()
        actionParentIDList.clear()
        actionParent = null
        arguments?.let {
            selectedDocList = it.getParcelableArrayList<Parcelable>(ARG_DOC_LIST) as ArrayList<Int>
            selectedCatList = it.getParcelableArrayList<Parcelable>(ARG_CAT_LIST) as ArrayList<Int>
            action = it.getString(ARG_ACTION)
            cta.text = action
            actionFirstParent = true
        }

        progressbar = Progressbar(requireContext())
        progressbar?.showPopup()
        view?.isFocusableInTouchMode = true
        view?.requestFocus()

        view?.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent): Boolean {
                if (event.action === KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    onBackPress()
                    return true

                }
                return false
            }
        })

        back.setOnClickListener {
            onBackPress()
        }

        docPath = "${resources.getString(R.string.document)}"
        actionPathList.add(docPath)

        mRecycler.layoutManager = LinearLayoutManager(context)
        adapter = CategoryOperationAdapter(requireContext(), arrayListOf(), this)
        mRecycler.adapter = adapter

        cta.setOnClickListener {
            actionParent = actionParentIDList[actionParentIDList.size - 1]
            if (action == resources.getString(R.string.copy_here)) {
                viewModel.copyDocumentandCategory(
                    actionParentIDList[actionParentIDList.size - 1],
                    selectedDocList,
                    selectedCatList
                )
            } else {
                viewModel.moveDocumentandCategory(
                    actionParentIDList[actionParentIDList.size - 1],
                    selectedDocList,
                    selectedCatList
                )
            }
        }
    }

    fun onBackPress() {
        categoryMainList.clear()

        if (actionFirstParent) {
            cta.isVisible = false
            onParentBackPress()
        } else {
            cta.isVisible = true
            progressbar?.showPopup()
            if (actionParentIDList.size > 1) {
                viewModel.fetchCategories(actionParentIDList[actionParentIDList.size - 2])

                actionParentIDList.removeAt(actionParentIDList.size - 1)
                actionPathList.removeAt(actionPathList.size - 1)
                setPath()

            } else if (actionParentIDList.size == 1) {
                viewModel.fetchCategories(null)
                actionFirstParent = true
                actionPathList.clear()
                actionParentIDList.clear()
                docPath = "${resources.getString(R.string.document)}"
                actionPathList.add(docPath)
                mPath.text = actionPathList[0]
            }
        }

    }

    override fun setObservers() {
        viewModel.categoriesLiveData.observe(viewLifecycleOwner) { categoryList ->
            categoryMainList.clear()
            if (actionFirstParent) {
                mPath.isVisible = false
                cta.isVisible = false
            } else {
                cta.isVisible = true
            }
            if (!categoryList.isNullOrEmpty()) {
                categoryMainList = categoryList
            }
            categoryMainList.add(
                0,
                Categories(resources.getString(R.string.add_folder), null, null, null, null)
            )
            adapter.setItems(categoryMainList)
            progressbar?.dismissPopup()
        }

        viewModel.actionStream.observe(viewLifecycleOwner) {
            when (it) {
                is DocumentOperationViewModel.ACTION.OnBackOfSuccess -> {
                    onParentBackPress()
                }
            }
        }
    }

    private fun onParentBackPress() {
        progressbar?.dismissPopup()
        var parentFrag: DocumentsFragment? = null
        parentFrag = this@CopyMoveFragment.parentFragment as DocumentsFragment
        parentFrag.onBackPress()
    }

    override fun onItemClick(item: Categories) {
        if (item.id == null) {
            createFolderDialog()
        } else {
            adapter.clearAll()
            if (actionFirstParent) {
                actionFirstParent = false
                mPath.isVisible = true
                cta.isVisible = false
            } else {
                cta.isVisible = true
            }
            item.id.let { actionParentIDList.add(it) }

            progressbar?.showPopup()
            viewModel.fetchCategories(item.id)

            actionPathList.add(item.name)
            setPath()
        }
    }

    private fun setPath() {

        docPath = ""
        if (actionPathList.size > 3) {
            docPath += actionPathList[0] + actionPathList[1] + " > " + " ... " + " > " + actionPathList[actionPathList.size - 1]

        } else {
            for (i in actionPathList.indices) {
                docPath += if (i == 0) {
                    actionPathList[i]
                } else {
                    " > " + actionPathList[i]
                }
            }
        }

        val switchDescriptionSpannable = SpannableString(docPath)
        val switchCtaStr = actionPathList[actionPathList.size - 1]
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

    private fun createFolderDialog() {
        val view = layoutInflater.inflate(R.layout.create_folder_dialog, null)
        dialogCreateCategory = BottomSheetDialog(requireContext())
        dialogCreateCategory?.setCancelable(true)
        dialogCreateCategory?.setContentView(view)
        val title = view.findViewById<TextView>(R.id.title)
        val catName = view.findViewById<EditText>(R.id.catName)
        val cancel = view.findViewById<ImageView>(R.id.cancel)
        val done = view.findViewById<ImageView>(R.id.done)

        title.text = resources.getString(R.string.create_new_category)

        cancel.setOnClickListener {
            catName.setText("")
        }

        done.setOnClickListener {
            progressbar?.showPopup()
            val result = checkNewNameExist(catName.text.toString())
            if (result) {
                val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
                val dialog: AlertDialog = builder.setTitle(resources.getString(R.string.app_name))
                    .setMessage(resources.getString(R.string.folder_exist))
                    .setPositiveButton(resources.getString(R.string.yes)) { dialog, _ ->
                        index = 1
                        changeName(catName.text.toString(), catName.text.toString(), false)
                        dialog.dismiss()
                    }
                    .setNegativeButton(resources.getString(R.string.no)) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()
                dialog.setCancelable(false)
                dialog.show()
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE)
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.DKGRAY)
            } else {
                if (actionParentIDList.isNotEmpty())
                    viewModel.addCategory(
                        actionParentIDList[actionParentIDList.size - 1],
                        catName.text.toString()
                    )
                else
                    viewModel.addCategory(null, catName.text.toString())
            }
            dialogCreateCategory?.dismiss()
        }
        dialogCreateCategory?.show()
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

    private fun changeName(originalName: String?, newName: String, recursiveCall: Boolean) {
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
        if (result) {
            changeName(originalName, categoryName, true)
        } else {
            if (actionParentIDList.isNotEmpty())
                viewModel.addCategory(
                    actionParentIDList[actionParentIDList.size - 1],
                    categoryName
                )
            else
                viewModel.addCategory(null, categoryName)
        }
    }


}