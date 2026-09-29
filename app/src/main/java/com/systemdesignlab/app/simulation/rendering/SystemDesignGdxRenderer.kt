package com.systemdesignlab.app.simulation.rendering

import com.badlogic.gdx.ApplicationListener
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.PerspectiveCamera
import com.badlogic.gdx.graphics.VertexAttributes.Usage
import com.badlogic.gdx.graphics.g3d.*
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder
import com.badlogic.gdx.math.Vector3
import com.systemdesignlab.app.simulation.engine.*

class SystemDesignGdxRenderer(
    val engine: SystemSimulationEngine
) : ApplicationListener {

    private lateinit var camera: PerspectiveCamera
    private lateinit var modelBatch: ModelBatch
    private lateinit var environment: Environment
    private val instances = mutableListOf<ModelInstance>()
    private val models = mutableListOf<Model>()

    private var boxModel: Model? = null
    private var cylinderModel: Model? = null
    private var packetModel: Model? = null

    // Camera orbit controls
    private var lastX = 0f
    private var lastY = 0f
    private var isDragging = false
    private var cameraDistance = 7.5f
    private var cameraAngleX = 25f
    private var cameraAngleY = 0f

    override fun create() {
        camera = PerspectiveCamera(60f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat()).apply {
            position.set(0f, 4f, cameraDistance)
            lookAt(0f, 0f, 0f)
            near = 0.5f
            far = 100f
            update()
        }

        modelBatch = ModelBatch()
        environment = Environment().apply {
            set(ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.45f, 0.5f, 1f))
            add(DirectionalLight().set(0.8f, 0.85f, 0.9f, -0.6f, -1.0f, -0.8f))
        }

        val modelBuilder = ModelBuilder()

        // Server / Box model
        boxModel = modelBuilder.createBox(
            0.6f, 0.9f, 0.6f,
            Material(ColorAttribute.createDiffuse(Color(0.15f, 0.25f, 0.45f, 1f))),
            (Usage.Position or Usage.Normal).toLong()
        ).also { models.add(it) }

        // Database / Cylinder model
        cylinderModel = modelBuilder.createCylinder(
            0.7f, 0.8f, 0.7f, 16,
            Material(ColorAttribute.createDiffuse(Color(0.45f, 0.2f, 0.65f, 1f))),
            (Usage.Position or Usage.Normal).toLong()
        ).also { models.add(it) }

        // Packet / Data cube
        packetModel = modelBuilder.createBox(
            0.15f, 0.15f, 0.15f,
            Material(ColorAttribute.createDiffuse(Color(0.2f, 0.85f, 0.45f, 1f))),
            (Usage.Position or Usage.Normal).toLong()
        ).also { models.add(it) }
    }

    override fun resize(width: Int, height: Int) {
        camera.viewportWidth = width.toFloat()
        camera.viewportHeight = height.toFloat()
        camera.update()
    }

    override fun render() {
        val delta = Gdx.graphics.deltaTime
        engine.update(delta)

        handleTouchInput()

        // Clear screen with premium dark tech slate
        Gdx.gl.glViewport(0, 0, Gdx.graphics.width, Gdx.graphics.height)
        Gdx.gl.glClearColor(0.06f, 0.08f, 0.12f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)

        camera.update()

        instances.clear()

        // 1. Build instances for nodes
        val nodeMap = engine.nodes.associateBy { it.id }
        for (node in engine.nodes) {
            val baseModel = when (node.type) {
                SimNodeType.DATABASE, SimNodeType.REPLICA -> cylinderModel ?: continue
                else -> boxModel ?: continue
            }

            val instance = ModelInstance(baseModel).apply {
                transform.setToTranslation(node.x, node.y, node.z)
            }

            // Color tint based on status
            val colorAttr = instance.materials.first().get(ColorAttribute.Diffuse) as? ColorAttribute
            when (node.status) {
                SimNodeStatus.FAILED -> colorAttr?.color?.set(Color.DARK_GRAY)
                SimNodeStatus.OVERLOADED -> colorAttr?.color?.set(Color.RED)
                SimNodeStatus.DEGRADED -> colorAttr?.color?.set(Color.ORANGE)
                SimNodeStatus.HEALTHY -> {
                    when (node.type) {
                        SimNodeType.CLIENT -> colorAttr?.color?.set(Color(0.2f, 0.6f, 0.9f, 1f))
                        SimNodeType.LOAD_BALANCER -> colorAttr?.color?.set(Color(0.1f, 0.75f, 0.7f, 1f))
                        SimNodeType.CACHE -> colorAttr?.color?.set(Color(0.9f, 0.6f, 0.1f, 1f))
                        SimNodeType.DATABASE -> colorAttr?.color?.set(Color(0.6f, 0.3f, 0.85f, 1f))
                        SimNodeType.QUEUE -> colorAttr?.color?.set(Color(0.85f, 0.45f, 0.15f, 1f))
                        else -> colorAttr?.color?.set(Color(0.25f, 0.5f, 0.95f, 1f))
                    }
                }
            }
            instances.add(instance)
        }

        // 2. Build instances for moving packets
        for (packet in engine.packets) {
            val from = nodeMap[packet.fromNodeId] ?: continue
            val to = nodeMap[packet.toNodeId] ?: continue

            val px = from.x + (to.x - from.x) * packet.progress
            val py = from.y + (to.y - from.y) * packet.progress
            val pz = from.z + (to.z - from.z) * packet.progress

            val pkt = ModelInstance(packetModel ?: continue).apply {
                transform.setToTranslation(px, py, pz)
            }

            val pktColor = pkt.materials.first().get(ColorAttribute.Diffuse) as? ColorAttribute
            if (packet.isError) {
                pktColor?.color?.set(Color.RED)
            } else if (packet.isCacheHit) {
                pktColor?.color?.set(Color.GREEN)
            } else {
                pktColor?.color?.set(Color.CYAN)
            }
            instances.add(pkt)
        }

        modelBatch.begin(camera)
        modelBatch.render(instances, environment)
        modelBatch.end()
    }

    private fun handleTouchInput() {
        if (Gdx.input.isTouched) {
            val x = Gdx.input.x.toFloat()
            val y = Gdx.input.y.toFloat()

            if (isDragging) {
                val dx = x - lastX
                val dy = y - lastY

                cameraAngleY += dx * 0.3f
                cameraAngleX = (cameraAngleX + dy * 0.3f).coerceIn(5f, 80f)

                updateCameraPosition()
            }
            lastX = x
            lastY = y
            isDragging = true
        } else {
            isDragging = false
        }
    }

    private fun updateCameraPosition() {
        val radX = Math.toRadians(cameraAngleX.toDouble())
        val radY = Math.toRadians(cameraAngleY.toDouble())

        val cx = (cameraDistance * Math.cos(radX) * Math.sin(radY)).toFloat()
        val cy = (cameraDistance * Math.sin(radX)).toFloat()
        val cz = (cameraDistance * Math.cos(radX) * Math.cos(radY)).toFloat()

        camera.position.set(cx, cy, cz)
        camera.lookAt(0f, 0f, 0f)
        camera.up.set(Vector3.Y)
    }

    override fun pause() {}
    override fun resume() {}

    override fun dispose() {
        modelBatch.dispose()
        for (m in models) {
            m.dispose()
        }
        models.clear()
    }
}
