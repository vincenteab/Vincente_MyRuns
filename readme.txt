Vincente Buenaventura
301422086

I have used, implemented, and built on top of the code below, that was shown in lecture, in my assignment 3.


class MyListAdapter(private val context: Context, private var commentList: List<Comment>) : BaseAdapter(){

    override fun getItem(position: Int): Any {
        return commentList.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return commentList.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = View.inflate(context, R.layout.layout_adapter,null)

        val textViewID = view.findViewById(R.id.tv_number) as TextView
        val textViewComment = view.findViewById(R.id.tv_string) as TextView

        textViewID.text = commentList.get(position).id.toString()
        textViewComment.text = commentList.get(position).comment

        return view
    }

    fun replace(newCommentList: List<Comment>){
        commentList = newCommentList
    }

}

@Database(entities = [Comment::class], version = 1)
abstract class CommentDatabase : RoomDatabase() { //XD: Room automatically generates implementations of your abstract CommentDatabase class.
    abstract val commentDatabaseDao: CommentDatabaseDao

    companion object{
        //The Volatile keyword guarantees visibility of changes to the INSTANCE variable across threads
        @Volatile
        private var INSTANCE: CommentDatabase? = null

        fun getInstance(context: Context) : CommentDatabase{
            synchronized(this){
                var instance = INSTANCE
                if(instance == null){
                    instance = Room.databaseBuilder(context.applicationContext,
                        CommentDatabase::class.java, "comment_table").build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}

//A Repository manages queries and allows you to use multiple backends.
// In the most common example, the Repository implements the logic for
// deciding whether to fetch data from a network or use results cached in a local database.
class CommentRepository(private val commentDatabaseDao: CommentDatabaseDao) {

    val allComments: Flow<List<Comment>> = commentDatabaseDao.getAllComments()

    fun insert(comment: Comment){
        CoroutineScope(IO).launch{
            commentDatabaseDao.insertComment(comment)
        }
    }

    fun delete(id: Long){
        CoroutineScope(IO).launch {
            commentDatabaseDao.deleteComment(id)
        }
    }

    fun deleteAll(){
        CoroutineScope(IO).launch {
            commentDatabaseDao.deleteAll()
        }
    }
}

//XD: At compile time, Room automatically generates implementations of the custom DAO interface that you define.
// Click the middle mouse button (Windows users) to see the code generated automatically by the system.
@Dao
interface CommentDatabaseDao {

    @Insert
    suspend fun insertComment(comment: Comment)

    //A Flow is an async sequence of values
    //Flow produces values one at a time (instead of all at once) that can generate values
    //from async operations like network requests, database calls, or other async code.
    //It supports coroutines throughout its API, so you can transform a flow using coroutines as well!
    //Code inside the flow { ... } builder block can suspend. So the function is no longer marked with suspend modifier.
    //See more details here: https://kotlinlang.org/docs/flow.html#flows
    @Query("SELECT * FROM comment_table")
    fun getAllComments(): Flow<List<Comment>>

    @Query("DELETE FROM comment_table")
    suspend fun deleteAll()

    @Query("DELETE FROM comment_table WHERE id = :key") //":" indicates that it is a Bind variable
    suspend fun deleteComment(key: Long)
}


@Entity(tableName = "comment_table")
data class Comment (
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,

    @ColumnInfo(name = "comment_column")
    var comment: String = ""

)

class CommentViewModel(private val repository: CommentRepository) : ViewModel() {
    val allCommentsLiveData: LiveData<List<Comment>> = repository.allComments.asLiveData()

    fun insert(comment: Comment) {
        repository.insert(comment)
    }

    fun deleteFirst(){
        val commentList = allCommentsLiveData.value
        if (commentList != null && commentList.size > 0){
            val id = commentList[0].id
            repository.delete(id)
        }
    }

    fun deleteAll(){
        val commentList = allCommentsLiveData.value
        if (commentList != null && commentList.size > 0)
            repository.deleteAll()
    }
}

class CommentViewModelFactory (private val repository: CommentRepository) : ViewModelProvider.Factory {
    override fun<T: ViewModel> create(modelClass: Class<T>) : T{ //create() creates a new instance of the modelClass, which is CommentViewModel in this case.
        if(modelClass.isAssignableFrom(CommentViewModel::class.java))
            return CommentViewModel(repository) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


class MyListFragment : Fragment() {
    private val comments = arrayOf(
        "Canada", "SFU", "CMPT362",
        "is", "is", "the", "best", "coolest", "place", "in", "the", "universe!"
    )
    private lateinit var addButton: Button
    private lateinit var deleteButton: Button
    private lateinit var deleteAllButton: Button
    private lateinit var myListView: ListView

    private lateinit var arrayList: ArrayList<Comment>
    private lateinit var arrayAdapter: MyListAdapter

    private lateinit var database: CommentDatabase
    private lateinit var databaseDao: CommentDatabaseDao
    private lateinit var repository: CommentRepository
    private lateinit var viewModelFactory: CommentViewModelFactory
    private lateinit var commentViewModel: CommentViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_my_list, container, false)
        myListView = view.findViewById(R.id.list)
        addButton = view.findViewById(R.id.add)
        deleteButton = view.findViewById(R.id.delete)
        deleteAllButton = view.findViewById(R.id.deleteall)

        arrayList = ArrayList()
        arrayAdapter = MyListAdapter(requireActivity(), arrayList)
        myListView.adapter = arrayAdapter

//        val application = requireNotNull(activity).application
        database = CommentDatabase.getInstance(requireActivity())
        databaseDao = database.commentDatabaseDao
        repository = CommentRepository(databaseDao)
        viewModelFactory = CommentViewModelFactory(repository)
        commentViewModel = ViewModelProvider(requireActivity(), viewModelFactory).get(CommentViewModel::class.java)

        commentViewModel.allCommentsLiveData.observe(requireActivity(), Observer { it ->
            arrayAdapter.replace(it)
            arrayAdapter.notifyDataSetChanged()
        })

        addButton.setOnClickListener(){
            val index = (0..comments.size-1).random()
            val comment = Comment()
            comment.comment = comments[index]
            commentViewModel.insert(comment)
        }

        deleteButton.setOnClickListener(){
            commentViewModel.deleteFirst()
        }

        deleteAllButton.setOnClickListener(){
            commentViewModel.deleteAll()
        }

        return view
    }


}