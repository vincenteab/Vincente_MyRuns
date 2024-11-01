Vincente Buenaventura
301422086

I have used, implemented, and built on top of the code below, that was shown in lecture, in my assignment 2.


class MyFragmentStateAdapter(activity: FragmentActivity, var list: ArrayList<Fragment>)
    : FragmentStateAdapter(activity){

    override fun createFragment(position: Int): Fragment {
        return list[position]
    }

    override fun getItemCount(): Int {
        return list.size
    }
}


class FragmentB : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_b, container, false)
    }

}


class DateAndTimeActivity : AppCompatActivity(),
    TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {
    private lateinit var textView: TextView
    private lateinit var dateButton:Button
    private lateinit var timeButton: Button

    //#2
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_date_and_time)
        textView = findViewById(R.id.dateTime)
        dateButton = findViewById(R.id.dateBtn)
        timeButton = findViewById(R.id.timeBtn)

        dateButton.setOnClickListener(){
            //#5
            val datePickerDialog = DatePickerDialog(
                this, this,calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        timeButton.setOnClickListener(){
            //#3
            val timePickerDialog = TimePickerDialog(
                this, this,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE), true
            )
            timePickerDialog.show()
        }
    }


    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        //#4
        textView.text = "$hourOfDay : $minute"
    }

    override fun onDateSet(view: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        //6
        textView.text = "${year.toString()} / ${monthOfYear + 1} / $dayOfMonth"
    }
}



class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

    }

    fun onLinearLayoutClicked(v: View) {
        if (v.id == R.id.btn_linear_layout) { //XD added
            val intent = Intent(this, LinearLayoutActivity::class.java)
            startActivity(intent)
            Toast.makeText(this, "onLinearoutClicked", Toast.LENGTH_SHORT).show();
        }
    }

    fun onRelativeLayoutClicked(v: View?) {
        val intent = Intent(this, RelativeLayoutActivity::class.java)
        startActivity(intent)
    }

    fun onScrollViewLayoutClicked(v: View?) {
        val intent = Intent(this, ScrollViewLayoutActivity::class.java)
        startActivity(intent)
    }

    fun onDateTimeLayoutClicked(v: View?) {
        val intent = Intent(this, DateAndTimeActivity::class.java)
        startActivity(intent)
    }

    fun onSharedPreferencesClicked(v: View?) {
        val intent = Intent(this, SharedPreferencesActivity::class.java)
        startActivity(intent)
    }

    fun onConstraintLayoutClicked(v: View?) {
        val intent = Intent(this, ConstraintLayoutActivity::class.java)
        startActivity(intent)
    }

    fun onListViewLayoutClicked(v: View?) {
        val intent = Intent(this, ListViewLayoutActivity::class.java)
        startActivity(intent)
    }
}


