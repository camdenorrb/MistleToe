package me.camdenorrb.mistletoe.vulkan

import me.camdenorrb.mistletoe.vulkan.ext.free
import me.camdenorrb.mistletoe.vulkan.ext.use
import org.lwjgl.PointerBuffer
import org.lwjgl.system.MemoryUtil.*
import org.lwjgl.vulkan.*
import org.lwjgl.vulkan.EXTDebugReport.*
import org.lwjgl.vulkan.KHRDisplaySwapchain.VK_ERROR_INCOMPATIBLE_DISPLAY_KHR
import org.lwjgl.vulkan.KHRSurface.*
import org.lwjgl.vulkan.KHRSwapchain.*
import org.lwjgl.vulkan.VK10.*
import java.nio.ByteBuffer
import org.lwjgl.vulkan.VK10.VK_SUCCESS
import org.lwjgl.system.MemoryUtil.memAllocLong
import org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO
import org.lwjgl.vulkan.VkShaderModuleCreateInfo
import org.lwjgl.vulkan.VkDevice
import java.nio.file.Files
import java.nio.file.Paths
import java.util.ArrayList


// TODO: Have a GUI interface which has a draw method and make this abstract
// TODO: The goal is to make an API as easy as Processing but not Processing
// https://github.com/LWJGL/lwjgl3-demos/blob/master/src/org/lwjgl/demo/vulkan/TriangleDemo.java
class VulkanGui {

    private var width = 0

    private var height = 0


    val validation = System.getProperty("vulkan.validation", "false")!!.toBoolean()


    fun createVulkanInstance(layers: Array<ByteBuffer>, requiredExt: PointerBuffer): VkInstance {

        val appInfo = VkApplicationInfo.calloc()
            .sType(VK_STRUCTURE_TYPE_APPLICATION_INFO)
            .pApplicationName(memUTF8("Vulkan Test"))
            .pEngineName(memUTF8(""))
            .apiVersion(VK_API_VERSION_1_0)

        val ppEnabledExtNames = memAllocPointer(requiredExt.remaining() + 1).apply {

            put(requiredExt)

            memUTF8(VK_EXT_DEBUG_REPORT_EXTENSION_NAME).use {
                put(it).flip()
            }
        }

        val ppEnabledLayerNames = memAllocPointer(layers.size)

        if (validation) {

            layers.forEach {
                ppEnabledLayerNames.put(it)
            }

            ppEnabledLayerNames.flip()
        }

        val pCreateInfo = VkInstanceCreateInfo.calloc()
            .sType(VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO)
            .pNext(NULL)
            .pApplicationInfo(appInfo)
            .ppEnabledExtensionNames(ppEnabledExtNames)
            .ppEnabledLayerNames(ppEnabledLayerNames)

        val vkInstance = memAllocPointer(1).use { pInstance ->

            val response = vkCreateInstance(pCreateInfo, null, pInstance)
            val instance = pInstance.get(0)

            assert(response == VK_SUCCESS) {
                "Failed to create VkInstance: ${translateVulkanResult(response)}"
            }

            VkInstance(instance, pCreateInfo)
        }

        ppEnabledExtNames.free()
        ppEnabledLayerNames.free()

        appInfo.pEngineName()!!.free()
        appInfo.pApplicationName()!!.free()
        appInfo.free()

        return vkInstance
    }

    fun setupDebugging(instance: VkInstance, flags: Int, callback: VkDebugReportCallbackEXT): Long {

        val debugCreateInfo = VkDebugReportCallbackCreateInfoEXT.calloc()
            .sType(VK_STRUCTURE_TYPE_DEBUG_REPORT_CALLBACK_CREATE_INFO_EXT)
            .pNext(NULL)
            .pfnCallback(callback)
            .pUserData(NULL)
            .flags(flags)

        val callbackHandle = memAllocLong(1).use { pCallback ->

            val response = vkCreateDebugReportCallbackEXT(instance, debugCreateInfo, null, pCallback)

            assert(response == VK_SUCCESS) {
                "Failed to create VkInstance: ${translateVulkanResult(response)}"
            }

            pCallback.get(0)
        }

        debugCreateInfo.free()

        return callbackHandle
    }

    fun firstPhysicalDevice(instance: VkInstance): VkPhysicalDevice {

        val pPhysicalDeviceCount = memAllocInt(1)

        var response = vkEnumeratePhysicalDevices(instance, pPhysicalDeviceCount, null)

        assert(response == VK_SUCCESS) {
            "Failed to get number of physical devices: ${translateVulkanResult(response)}"
        }

        val pPhysicalDevices = memAllocPointer(pPhysicalDeviceCount.get(0))

        response = vkEnumeratePhysicalDevices(instance, pPhysicalDeviceCount, pPhysicalDevices)

        assert(response == VK_SUCCESS) {
            "Failed to get physical devices: ${translateVulkanResult(response)}"
        }

        val physicalDevice = pPhysicalDevices.get(0)

        pPhysicalDevices.free()
        pPhysicalDeviceCount.free()

        return VkPhysicalDevice(physicalDevice, instance)
    }

    fun createDeviceAndGetGraphicQueueFamily(physicalDevice: VkPhysicalDevice, layers: Array<ByteBuffer>): DeviceAndGraphicsQueueFamily {

        val graphicsQueueFamilyIndex = memAllocInt(1).use { pQueueFamilyPropertyCount ->

            vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, pQueueFamilyPropertyCount, null)

            val queueCount = pQueueFamilyPropertyCount.get(0)

            val queueProps = VkQueueFamilyProperties.calloc(queueCount)

            vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, pQueueFamilyPropertyCount, queueProps)

            for (i in 0 until queueCount) {
                if (queueProps.get(i).queueFlags() and VK_QUEUE_GRAPHICS_BIT != 0) {
                    queueProps.free()
                    return@use i
                }
            }

            return@use 0
        }

        val pQueuePriorities = memAllocFloat(1).put(0.0f).flip()

        val queueCreateInfo = VkDeviceQueueCreateInfo.calloc(1)
            .sType(VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO)
            .queueFamilyIndex(graphicsQueueFamilyIndex)
            .pQueuePriorities(pQueuePriorities)

        val extensions = memUTF8(VK_KHR_SWAPCHAIN_EXTENSION_NAME).use {
            memAllocPointer(1).put(it).flip()
        }

        val ppEnabledLayerNames = memAllocPointer(layers.size)

        if (validation) {

            layers.forEach {
                ppEnabledLayerNames.put(it)
            }

            ppEnabledLayerNames.flip()
        }

        val deviceCreateInfo = VkDeviceCreateInfo.calloc()
            .sType(VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO)
            .pNext(NULL)
            .pQueueCreateInfos(queueCreateInfo)
            .ppEnabledExtensionNames(extensions)
            .ppEnabledLayerNames(ppEnabledLayerNames)

        val device = memAllocPointer(1).use { pDevice ->

            val response = vkCreateDevice(physicalDevice, deviceCreateInfo, null, pDevice)

            assert(response == VK_SUCCESS) {
                "Failed to create device: ${translateVulkanResult(response)}"
            }

            pDevice.get(0)
        }

        val memoryProperties = VkPhysicalDeviceMemoryProperties.calloc()
        vkGetPhysicalDeviceMemoryProperties(physicalDevice, memoryProperties)

        val deviceAndGraphicsQueueFamily = DeviceAndGraphicsQueueFamily(VkDevice(device, physicalDevice, deviceCreateInfo), graphicsQueueFamilyIndex, memoryProperties)

        extensions.free()
        pQueuePriorities.free()
        deviceCreateInfo.free()
        ppEnabledLayerNames.free()

        return deviceAndGraphicsQueueFamily
    }

    fun getColorFormatAndSpace(physicalDevice: VkPhysicalDevice, surface: Long): ColorFormatAndSpace {

        val pQueueFamilyPropertyCount = memAllocInt(1)

        vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, pQueueFamilyPropertyCount, null)

        val queueCount = pQueueFamilyPropertyCount.get(0)
        val queueProps = VkQueueFamilyProperties.calloc(queueCount)

        vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, pQueueFamilyPropertyCount, queueProps)

        val supportsPresent = memAllocInt(queueCount)

        repeat(queueCount) {

            supportsPresent.position(it)

            val result = vkGetPhysicalDeviceSurfaceSupportKHR(physicalDevice, it, surface, supportsPresent)

            assert(result == VK_SUCCESS) {
                "Failed to physical device surface support: ${translateVulkanResult(result)}"
            }
        }

        var graphicsQueueNodeIndex = Int.MAX_VALUE
        var presentQueueNodeIndex = Int.MAX_VALUE

        for (i in 0 until queueCount) {

            if ((queueProps.get(i).queueFlags() and VK_QUEUE_GRAPHICS_BIT) != 0) {

                if (graphicsQueueNodeIndex == Int.MAX_VALUE) {
                    graphicsQueueNodeIndex = i
                }

                if (supportsPresent.get(i) == VK_TRUE) {
                    graphicsQueueNodeIndex = i
                    presentQueueNodeIndex = i
                    break
                }

            }

        }

        if (presentQueueNodeIndex == Int.MAX_VALUE) {
            for (i in 0 until queueCount) {
                if (supportsPresent.get(i) == VK_TRUE) {
                    presentQueueNodeIndex = i
                    break
                }
            }
        }

        assert(graphicsQueueNodeIndex != Int.MAX_VALUE) {
            "No graphics queue found"
        }

        assert(presentQueueNodeIndex != Int.MAX_VALUE) {
            "No presentation queue found"
        }

        assert(graphicsQueueNodeIndex == presentQueueNodeIndex) {
            "Presentation queue != graphics queue"
        }


        val pFormatCount = memAllocInt(1)

        var response = vkGetPhysicalDeviceSurfaceFormatsKHR(physicalDevice, surface, pFormatCount, null)
        val formatCount = pFormatCount[1]

        assert(response == VK_SUCCESS) {
            "[1] Failed to query number of physical device surface formats: ${translateVulkanResult(response)}"
        }


        val surfFormats = VkSurfaceFormatKHR.calloc(formatCount)
        response = vkGetPhysicalDeviceSurfaceFormatsKHR(physicalDevice, surface, pFormatCount, surfFormats)

        assert(response == VK_SUCCESS) {
            "[2] Failed to query number of physical device surface formats: ${translateVulkanResult(response)}"
        }

        val colorFormat =
            if (formatCount == 1 && surfFormats[0].format() == VK_FORMAT_UNDEFINED) {
                VK_FORMAT_B8G8R8A8_UNORM
            }
            else {
                surfFormats[0].format()
            }

        val colorSpace = surfFormats[0].colorSpace()

        queueProps.free()
        surfFormats.free()
        pFormatCount.free()
        supportsPresent.free()
        pQueueFamilyPropertyCount.free()

        return ColorFormatAndSpace(colorFormat, colorSpace)
    }

    fun createCommandPool(device: VkDevice, queueNodeIndex: Int): Long {

        val cmdPoolInfo = VkCommandPoolCreateInfo.calloc()
            .sType(VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO)
            .queueFamilyIndex(queueNodeIndex)
            .flags(VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT)

        val cmdPool = memAllocLong(1).use { pCmdPool ->

            val response = vkCreateCommandPool(device, cmdPoolInfo, null, pCmdPool)

            assert(response == VK_SUCCESS) {
                "Failed to create command pool: ${translateVulkanResult(response)}"
            }

            return@use pCmdPool[0]
        }

        cmdPoolInfo.free()

        return cmdPool
    }

    fun createDeviceQueue(device: VkDevice, queueFamilyIndex: Int): VkQueue {

        val queue = memAllocPointer(1).use { pQueue ->
            vkGetDeviceQueue(device, queueFamilyIndex, 0, pQueue)
            return@use pQueue[0]
        }

        return VkQueue(queue, device)
    }

    fun createCommandBuffer(device: VkDevice, commandPool: Long): VkCommandBuffer {

        val cmdBufferAllocateInfo = VkCommandBufferAllocateInfo.calloc()
            .sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO)
            .commandPool(commandPool)
            .level(VK_COMMAND_BUFFER_LEVEL_PRIMARY)
            .commandBufferCount(1)

        val commandBuffer = memAllocPointer(1).use { pCommandBuffer ->

            val response = vkAllocateCommandBuffers(device, cmdBufferAllocateInfo, pCommandBuffer)

            assert(response == VK_SUCCESS) {
                "Failed to allocate command buffer: ${translateVulkanResult(response)}"
            }

            pCommandBuffer[0]
        }

        cmdBufferAllocateInfo.free()

        return VkCommandBuffer(commandBuffer, device)
    }

    fun createSwapChain(device: VkDevice, physicalDevice: VkPhysicalDevice, surface: Long, oldSwapChain: Long, commandBuffer: VkCommandBuffer, newWidth: Int, newHeight: Int, colorFormat: Int, colorSpace: Int): SwapChain {

        val surfCaps = VkSurfaceCapabilitiesKHR.calloc()
        var response = vkGetPhysicalDeviceSurfaceCapabilitiesKHR(physicalDevice, surface, surfCaps)

        assert(response == VK_SUCCESS) {
            "Failed to get physical device surface capabilities: ${translateVulkanResult(response)}"
        }

        val pPresentModeCount = memAllocInt(1)

        response = vkGetPhysicalDeviceSurfacePresentModesKHR(physicalDevice, surface, pPresentModeCount, null)

        assert(response == VK_SUCCESS) {
            "[1] Failed to get the number of physical device surface presentation modes: ${translateVulkanResult(response)}"
        }

        val presentModeCount =  pPresentModeCount[0]

        val swapChainPresetMode = memAllocInt(presentModeCount).use { pPresentModes ->

            response = vkGetPhysicalDeviceSurfacePresentModesKHR(physicalDevice, surface, pPresentModeCount, pPresentModes)

            assert(response == VK_SUCCESS) {
                "[2] Failed to get the number of physical device surface presentation modes: ${translateVulkanResult(response)}"
            }

            var swapChainPresetMode = VK_PRESENT_MODE_FIFO_KHR

            for (i in 0 until presentModeCount) {
                if (pPresentModes[i] == VK_PRESENT_MODE_MAILBOX_KHR) {
                    swapChainPresetMode = VK_PRESENT_MODE_MAILBOX_KHR
                    break
                }
                if (pPresentModes[i] == VK_PRESENT_MODE_IMMEDIATE_KHR) {
                    swapChainPresetMode = VK_PRESENT_MODE_IMMEDIATE_KHR
                }
            }

            return@use swapChainPresetMode
        }

        var desiredNumberOfSwapchainImages = surfCaps.minImageCount() + 1


        if (surfCaps.maxImageCount() in 1 until desiredNumberOfSwapchainImages) {
            desiredNumberOfSwapchainImages = surfCaps.maxImageCount()
        }

        val currentExtent = surfCaps.currentExtent()

        val currentWidth = currentExtent.width()

        val currentHeight = currentExtent.height()


        if (currentWidth != -1 && currentHeight != -1) {
            width = currentWidth
            height = currentHeight
        }
        else {
            width = newWidth
            height = newHeight
        }

        val preTransform =
                if (surfCaps.supportedTransforms() and VK_SURFACE_TRANSFORM_IDENTITY_BIT_KHR != 0) {
                    VK_SURFACE_TRANSFORM_IDENTITY_BIT_KHR
                }
                else {
                    surfCaps.currentTransform()
                }

        val swapChainCI = VkSwapchainCreateInfoKHR.calloc()
                .sType(VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR)
                .pNext(NULL)
                .surface(surface)
                .minImageCount(desiredNumberOfSwapchainImages)
                .imageFormat(colorFormat)
                .imageColorSpace(colorSpace)
                .imageUsage(VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT)
                .preTransform(preTransform)
                .imageArrayLayers(1)
                .imageSharingMode(VK_SHARING_MODE_EXCLUSIVE)
                .pQueueFamilyIndices(null)
                .presentMode(swapChainPresetMode)
                .oldSwapchain(oldSwapChain)
                .clipped(true)
                .compositeAlpha(VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR)

        swapChainCI.imageExtent().width(width).height(height)

       val swapChain =  memAllocLong(1).use { pSwapChain ->

           response = vkCreateSwapchainKHR(device, swapChainCI, null, pSwapChain)

           assert(response == VK_SUCCESS) {
               "Failed to create swap chain: ${translateVulkanResult(response)}"
           }

           return@use pSwapChain[0]
        }


        if (oldSwapChain != VK_NULL_HANDLE) {
            vkDestroySwapchainKHR(device, oldSwapChain, null)
        }

        val pImageCount = memAllocInt(1)

        response = vkGetSwapchainImagesKHR(device, swapChain, pImageCount, null)

        assert(response == VK_SUCCESS) {
            "Failed to get number of swap chain images: ${translateVulkanResult(response)}"
        }


        val imageCount = pImageCount[0]
        val pSwapChainImages = memAllocLong(imageCount)

        response = vkGetSwapchainImagesKHR(device, swapChain, pImageCount, pSwapChainImages)

        assert(response == VK_SUCCESS) {
            "Failed to get swap chain images: ${translateVulkanResult(response)}"
        }

        pImageCount.free()

        val images = LongArray(imageCount)
        val imageViews = LongArray(imageCount)
        val pBufferView = memAllocLong(1)

        val colorAttachmentView = VkImageViewCreateInfo.calloc()
                .sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO)
                .pNext(NULL)
                .format(colorFormat)
                .viewType(VK_IMAGE_VIEW_TYPE_2D)
                .flags(0) // None

        colorAttachmentView.components()
                .r(VK_COMPONENT_SWIZZLE_IDENTITY)
                .g(VK_COMPONENT_SWIZZLE_IDENTITY)
                .b(VK_COMPONENT_SWIZZLE_IDENTITY)
                .a(VK_COMPONENT_SWIZZLE_IDENTITY)

        colorAttachmentView.subresourceRange()
                .aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
                .baseMipLevel(0)
                .levelCount(1)
                .baseArrayLayer(0)
                .layerCount(1)

        for (i in 0 until imageCount) {

            images[i] = pSwapChainImages.get(i)
            colorAttachmentView.image(images[i])

            response = vkCreateImageView(device, colorAttachmentView, null, pBufferView)

            imageViews[i] = pBufferView.get(0)

            assert(response == VK_SUCCESS) {
                throw AssertionError("Failed to create image view: " + translateVulkanResult(response))
            }

        }

        surfCaps.free()
        pBufferView.free()
        pSwapChainImages.free()
        pPresentModeCount.free()
        colorAttachmentView.free()

        return SwapChain(swapChain, images, imageViews)
    }

    fun createRenderPass(device: VkDevice, colorFormat: Int): Long {

        val attachments = VkAttachmentDescription.calloc(1)
                .format(colorFormat)
                .samples(VK_SAMPLE_COUNT_1_BIT)
                .loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR)
                .storeOp(VK_ATTACHMENT_STORE_OP_STORE)
                .stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE)
                .stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE)
                .initialLayout(VK_IMAGE_LAYOUT_UNDEFINED)
                .finalLayout(VK_IMAGE_LAYOUT_PRESENT_SRC_KHR)

        val colorReference = VkAttachmentReference.calloc(1)
                .attachment(0)
                .layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)

        val subpass = VkSubpassDescription.calloc(1)
                .pipelineBindPoint(VK_PIPELINE_BIND_POINT_GRAPHICS)
                .flags(0) // None
                .pInputAttachments(null)
                .colorAttachmentCount(colorReference.remaining())
                .pColorAttachments(colorReference) // <- only color attachment
                .pResolveAttachments(null)
                .pDepthStencilAttachment(null)
                .pPreserveAttachments(null)

        val renderPassInfo = VkRenderPassCreateInfo.calloc()
                .sType(VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO)
                .pNext(NULL)
                .pAttachments(attachments)
                .pSubpasses(subpass)
                .pDependencies(null)

        val pRenderPass = memAllocLong(1)
        val response = vkCreateRenderPass(device, renderPassInfo, null, pRenderPass)
        val renderPass = pRenderPass.get(0)

        assert(response == VK_SUCCESS) {
            "Failed to create clear render pass: " + translateVulkanResult(response)
        }

        subpass.free()
        attachments.free()
        pRenderPass.free()
        renderPassInfo.free()
        colorReference.free()

        return renderPass
    }

    private fun createFramebuffers(device: VkDevice, swapchain: SwapChain, renderPass: Long, width: Int, height: Int): LongArray {

        val attachments = memAllocLong(1)

        val fci = VkFramebufferCreateInfo.calloc()
                .sType(VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO)
                .pAttachments(attachments)
                .flags(0) // None
                .height(height)
                .width(width)
                .layers(1)
                .pNext(NULL)
                .renderPass(renderPass)

        // Create a framebuffer for each swap chain image
        val framebuffers = LongArray(swapchain.images.size)
        val pFramebuffer = memAllocLong(1)

        for (i in 0 until swapchain.images.size) {

            attachments.put(0, swapchain.imageViews[i])

            val response = vkCreateFramebuffer(device, fci, null, pFramebuffer)
            val framebuffer = pFramebuffer.get(0)

            assert(response == VK_SUCCESS) {
                "Failed to create framebuffer: ${translateVulkanResult(response)}"
            }

            framebuffers[i] = framebuffer
        }

        fci.free()
        attachments.free()
        pFramebuffer.free()

        return framebuffers
    }

    private fun submitCommandBuffer(queue: VkQueue, commandBuffer: VkCommandBuffer) {

        if (commandBuffer.address() == NULL) return

        val submitInfo = VkSubmitInfo.calloc().sType(VK_STRUCTURE_TYPE_SUBMIT_INFO)

        val pCommandBuffers = memAllocPointer(1).put(commandBuffer).flip()

        submitInfo.pCommandBuffers(pCommandBuffers)
        val response = vkQueueSubmit(queue, submitInfo, VK_NULL_HANDLE)

        submitInfo.free()
        pCommandBuffers.free()

        assert(response == VK_SUCCESS) {
            "Failed to submit command buffer: ${translateVulkanResult(response)}"
        }
    }

    private fun loadShader(classPath: String, device: VkDevice, stage: Int): Long {

        val shaderCode = glslToSpirv(classPath, stage)

        val moduleCreateInfo = VkShaderModuleCreateInfo.calloc().sType(VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO).pNext(NULL).pCode(shaderCode).flags(0)

        val pShaderModule = memAllocLong(1)
        val response = vkCreateShaderModule(device, moduleCreateInfo, null, pShaderModule)
        val shaderModule = pShaderModule[0]

        pShaderModule.free()

        if (response != VK_SUCCESS) {
            throw AssertionError("Failed to create shader module: " + translateVulkanResult(response))
        }

        return shaderModule
    }


    fun translateVulkanResult(result: Int): String {
        return when (result) {

            VK_SUCCESS -> "Command successfully completed."
            VK_NOT_READY -> "A fence or query has not yet completed."
            VK_TIMEOUT -> "A wait operation has not completed in the specified time."
            VK_EVENT_SET -> "An event is signaled."
            VK_EVENT_RESET -> "An event is unsignaled."
            VK_INCOMPLETE -> "A return array was too small for the result."
            VK_SUBOPTIMAL_KHR -> "A swapchain no longer matches the surface properties exactly, but can still be used to present to the surface successfully."

            VK_ERROR_OUT_OF_HOST_MEMORY -> "A host memory allocation has failed."
            VK_ERROR_OUT_OF_DEVICE_MEMORY -> "A device memory allocation has failed."
            VK_ERROR_INITIALIZATION_FAILED -> "Initialization of an object could not be completed for implementation-specific reasons."
            VK_ERROR_DEVICE_LOST -> "The logical or physical device has been lost."
            VK_ERROR_MEMORY_MAP_FAILED -> "Mapping of a memory object has failed."
            VK_ERROR_LAYER_NOT_PRESENT -> "A requested layer is not present or could not be loaded."
            VK_ERROR_EXTENSION_NOT_PRESENT -> "A requested extension is not supported."
            VK_ERROR_FEATURE_NOT_PRESENT -> "A requested feature is not supported."
            VK_ERROR_INCOMPATIBLE_DRIVER -> "The requested version of Vulkan is not supported by the driver or is otherwise incompatible for implementation-specific reasons."
            VK_ERROR_TOO_MANY_OBJECTS -> "Too many objects of the type have already been created."
            VK_ERROR_FORMAT_NOT_SUPPORTED -> "A requested format is not supported on this device."
            VK_ERROR_SURFACE_LOST_KHR -> "A surface is no longer available."
            VK_ERROR_NATIVE_WINDOW_IN_USE_KHR -> "The requested window is already connected to a VkSurfaceKHR, or to some other non-Vulkan API."
            VK_ERROR_OUT_OF_DATE_KHR -> """
                A surface has changed in such a way that it is no longer compatible with the swapchain, and further presentation requests using the 
                swapchain will fail. Applications must query the new surface properties and recreate their swapchain if they wish to continue
                presenting to the surface.
            """.trimIndent().replace("\n", "")
            VK_ERROR_INCOMPATIBLE_DISPLAY_KHR -> "The display used by a swapchain does not use the same presentable image layout, or is incompatible in a way that prevents sharing an" + " image."
            VK_ERROR_VALIDATION_FAILED_EXT -> "A validation layer found an error."

            else -> String.format("%s [%d]", "Unknown", result)
        }
    }

    class DeviceAndGraphicsQueueFamily internal constructor(val device: VkDevice, val queueFamilyIndex: Int, val memoryProperties: VkPhysicalDeviceMemoryProperties)

    class ColorFormatAndSpace internal constructor(val colorFormat: Int, val colorSpace: Int)

    class SwapChain internal constructor(val swapChainHandle: Long, val images: LongArray, val imageViews: LongArray)

    class Vertices internal constructor(val verticesBuf: Long, val createInfo: VkPipelineVertexInputStateCreateInfo)

}