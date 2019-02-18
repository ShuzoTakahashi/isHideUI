import com.leapmotion.leap.*
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

internal open class WithCoroutineListener : Listener(), CoroutineScope {

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job
}