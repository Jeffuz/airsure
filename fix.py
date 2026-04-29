import qai_hub as hub
import torch
from qai_hub_models.models.yolov11_det import Model
from qai_hub_models.utils.input_spec import make_torch_inputs

# 1. Get the model
print("Loading YOLOv11 model...")
model = Model.from_pretrained()
model.eval()

# 2. Prepare the inputs properly for torch.jit.trace
input_spec = model.get_input_spec()
torch_inputs = make_torch_inputs(input_spec)

# 3. Trace the model
print("Tracing model (converting to TorchScript)...")
traced_model = torch.jit.trace(model, torch_inputs, check_trace=False)

# 4. Submit directly to AI Hub
print("Submitting to AI Hub...")
compile_job = hub.submit_compile_job(
    model=traced_model,
    device=hub.Device("Samsung Galaxy S24 Ultra"),
    input_specs=input_spec,
    options="--target_runtime tflite"
)

print(f"Job submitted! Monitor it here: {compile_job.url}")

# .wait() returns the job object, which has a .success boolean
if compile_job.wait().success:
    target_model = compile_job.get_target_model()
    # Note: download() takes a directory or filename
    target_model.download("detector.tflite")
    print("Done! Model saved as detector.tflite")
else:
    print(f"Job failed! Check the logs here: {compile_job.url}")
