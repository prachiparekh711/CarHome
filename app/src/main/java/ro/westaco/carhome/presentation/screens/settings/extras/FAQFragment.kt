package ro.westaco.carhome.presentation.screens.settings.extras

import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_f_a_q.*
import ro.westaco.carhome.R
import ro.westaco.carhome.presentation.base.BaseFragment

//C- FAQ Section
@AndroidEntryPoint
class FAQFragment : BaseFragment<CommenViewModel>() {

    var faqFilterList: ArrayList<String> = ArrayList()
    var faqQuestionList: ArrayList<String> = ArrayList()
    var faqAnswerList: ArrayList<String> = ArrayList()


    private fun setTemporaryData() {
        faqFilterList.add("All")
        faqFilterList.add("Petrol")
        faqFilterList.add("Parking")
        faqFilterList.add("Car Wash")

        faqQuestionList.add("Q.1  Lorem ipsum dolor sit amet?")
        faqQuestionList.add("Q.2  Lorem ipsum dolor sit amet?")
        faqQuestionList.add("Q.3  Lorem ipsum dolor sit amet,consectetur adipiscing elit?")

        faqAnswerList.add("Q.1  Lorem ipsum dolor sit amet?")
        faqAnswerList.add("Q.2  Lorem ipsum dolor sit amet?")
        faqAnswerList.add("Q.3  Lorem ipsum dolor sit amet,consectetur adipiscing elit?")
    }

    override fun getContentView() = R.layout.fragment_f_a_q

    override fun initUi() {
        back.setOnClickListener {
            viewModel.onBack()
        }

        home.setOnClickListener {
            viewModel.onMain()
        }

        recycler?.layoutManager = LinearLayoutManager(
            requireActivity(),
            LinearLayoutManager.HORIZONTAL,
            false
        )

        faqRecycler?.layoutManager = LinearLayoutManager(
            requireActivity(),
            LinearLayoutManager.VERTICAL,
            false
        )

        faqRecycler?.adapter = FAQAdapter(
            requireActivity(),
            faqQuestionList, faqAnswerList
        )

        setTemporaryData()
        recycler?.adapter = FAQTabAdapter(
            requireActivity(),
            faqFilterList
        ) {
            faqRecycler?.adapter = FAQAdapter(
                requireActivity(),
                faqQuestionList, faqAnswerList
            )
        }


    }

    override fun setObservers() {
    }
}