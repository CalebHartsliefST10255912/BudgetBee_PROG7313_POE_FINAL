
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetbee_prog7313_poe_final.R
import com.example.budgetbee_prog7313_poe_final.model.Income

//This is the income adapter, it will help with the recylerView and formating for income
class IncomeAdapter : RecyclerView.Adapter<IncomeAdapter.IncomeViewHolder>() {

    private var incomes: List<Income> = listOf()

    inner class IncomeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val incomeName: TextView = itemView.findViewById(R.id.textIncomeName)
        val incomeAmount: TextView = itemView.findViewById(R.id.textIncomeAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncomeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_income, parent, false)
        return IncomeViewHolder(view)
    }

    override fun onBindViewHolder(holder: IncomeViewHolder, position: Int) {
        val income = incomes[position]
        holder.incomeName.text = income.name
        holder.incomeAmount.text = "R %.2f".format(income.amount)
    }

    override fun getItemCount(): Int = incomes.size

    fun submitList(list: List<Income>) {
        incomes = list
        notifyDataSetChanged()
    }
}


