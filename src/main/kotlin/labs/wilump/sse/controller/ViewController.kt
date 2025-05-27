package labs.wilump.sse.controller

import labs.wilump.sse.logger
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping



@Controller
class ViewController {

    @GetMapping("/")
    fun home(model: Model): String {
        return "index"
    }

    @GetMapping("/2")
    fun home2(model: Model): String {
        return "index2"
    }
}