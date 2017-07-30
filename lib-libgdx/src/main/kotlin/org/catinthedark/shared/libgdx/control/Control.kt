package org.catinthedark.shared.libgdx.control

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.controllers.Controller
import com.badlogic.gdx.controllers.ControllerAdapter
import com.badlogic.gdx.controllers.Controllers
import com.badlogic.gdx.controllers.PovDirection
import org.slf4j.LoggerFactory

object Control {

    enum class Type {
        KEYBOARD, CONTROLLER
    }

    enum class Button {
        UP, DOWN, LEFT, RIGHT, BUTTON0, BUTTON1, BUTTON2, BUTTON3
    }

    private val logger = LoggerFactory.getLogger(javaClass)

    private val types = mutableSetOf<Type>(Type.KEYBOARD)
    private var controller: Controller? = null

    init {
        val controllers = Controllers.getControllers()
        if (controllers.size > 0) {
            controller = controllers[0]
            types.add(Type.CONTROLLER)
            logger.info("Controller found: {}", controller?.name)
        }
        Controllers.addListener(object: ControllerAdapter() {
            override fun connected(controller: Controller?) {
                Control.controller = controller
                types.add(Type.CONTROLLER)
                logger.info("Controller connected: {}", Control.controller?.name)
            }

            override fun disconnected(controller: Controller?) {
                Control.controller = null
                types.remove(Type.CONTROLLER)
                logger.info("Controller disconnected: {}", controller?.name)
            }
        })
    }

    fun avaiableTypes(): Set<Type> {
        return types
    }

    fun pressed(): Set<Button> {
        val controllerPressed = controllerButtons(*Button.values())
        val keyboardPressed = keyboardButtons(*Button.values())
        return controllerPressed union keyboardPressed
    }

    fun isPressed(vararg buttons: Button): Boolean {
        val controllerPressed = buttons intersect controllerButtons(*buttons)
        val keyboardPressed = buttons intersect keyboardButtons(*buttons)
        return (controllerPressed union keyboardPressed).size == buttons.size
    }

    private fun controllerButtons(vararg buttons: Button): Set<Button> {
        if (controller == null) {
            return setOf()
        }
        val pressedButtons = mutableSetOf<Button>()

        val direction = controller?.getPov(0)
        pressedButtons.addAll(when (direction) {
            PovDirection.center -> setOf()
            PovDirection.east -> setOf(Button.RIGHT)
            PovDirection.north -> setOf(Button.UP)
            PovDirection.northEast -> setOf(Button.UP, Button.RIGHT)
            PovDirection.northWest -> setOf(Button.UP, Button.LEFT)
            PovDirection.south -> setOf(Button.DOWN)
            PovDirection.southEast -> setOf(Button.DOWN, Button.RIGHT)
            PovDirection.southWest -> setOf(Button.DOWN, Button.LEFT)
            PovDirection.west -> setOf(Button.LEFT)
            else -> setOf()
        })

        val axis0 = controller?.getAxis(0)
        val axis1 = controller?.getAxis(1)
        for (button in buttons) {
            when (button) {
                Button.LEFT -> if (axis0 != null && axis0 < -0.5) pressedButtons.add(button)
                Button.RIGHT -> if (axis0 != null && axis0 > 0.5) pressedButtons.add(button)
                Button.UP -> if (axis1 != null && axis1 < -0.5) pressedButtons.add(button)
                Button.DOWN -> if (axis1 != null && axis1 > 0.5) pressedButtons.add(button)
                else -> {}
            }
        }

        for (button in buttons) {
            val buttonCode = when(button) {
                Button.BUTTON0 -> 0
                Button.BUTTON1 -> 1
                Button.BUTTON2 -> 2
                Button.BUTTON3 -> 3
                else -> -1
            }
            if (controller?.getButton(buttonCode) ?: false) {
                pressedButtons.add(button)
            }
        }

        return pressedButtons
    }

    private fun keyboardButtons(vararg buttons: Button): Set<Button> {
        val pressedButtons = mutableSetOf<Button>()

        for (button in buttons) {
            val keyCodes = when (button) {
                Button.UP -> setOf(Input.Keys.W, Input.Keys.UP, Input.Keys.DPAD_UP)
                Button.DOWN -> setOf(Input.Keys.S, Input.Keys.DOWN, Input.Keys.DPAD_DOWN)
                Button.LEFT -> setOf(Input.Keys.A, Input.Keys.LEFT, Input.Keys.DPAD_LEFT)
                Button.RIGHT -> setOf(Input.Keys.D, Input.Keys.RIGHT, Input.Keys.DPAD_RIGHT)
                Button.BUTTON0 -> setOf(Input.Keys.SPACE, Input.Keys.DPAD_CENTER)
                Button.BUTTON1 -> setOf(Input.Keys.SHIFT_LEFT)
                Button.BUTTON2 -> setOf(Input.Keys.SHIFT_RIGHT)
                Button.BUTTON3 -> setOf(Input.Keys.ENTER)
            }
            for (keyCode in keyCodes) {
                if (Gdx.input.isKeyPressed(keyCode)) {
                    pressedButtons.add(button)
                }
            }
        }

        return pressedButtons
    }

}