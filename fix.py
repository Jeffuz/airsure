import qai_hub as hub
import torch
from qai_hub_models.models.yolov11_det import Model

print("Loading YOLOv11 model...")
qai_model = Model.from_pretrained()
raw_model = qai_model.model

# Wrapper to handle NHWC input [1, 640, 640, 3] and combine outputs
class YOLOAndroidWrapper(torch.nn.Module):
    def __init__(self, model):
        super().__init__()
        self.model = model

    def forward(self, x):
        # Permute NHWC (Android/TFLite) -> NCHW (PyTorch)
        x = x.permute(0, 3, 1, 2)
        # Model output is [boxes, scores]
        output = self.model(x)
        # Concatenate into [1, 84, 8400] for the Java code
        return torch.cat([output[0], output[1]], dim=1)

wrapped_model = YOLOAndroidWrapper(raw_model)
wrapped_model.eval()

print("Tracing model...")
# Create a dummy NHWC input [1, 640, 640, 3]
example_input = torch.randn(1, 640, 640, 3)
traced_model = torch.jit.trace(wrapped_model, example_input, check_trace=False)

print("Submitting to AI Hub...")
compile_job = hub.submit_compile_job(
    model=traced_model,
    device=hub.Device("Samsung Galaxy S24 Ultra"),
    input_specs=dict(image=(1, 640, 640, 3)),
    options="--target_runtime tflite"
)

print(f"Job submitted! Monitor it here: {compile_job.url}")
compile_job.wait()
compile_job.get_target_model().download("detector.tflite")
print("Done! Now move 'detector.tflite' to src/main/assets/ and redeploy.")
