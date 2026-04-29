import qai_hub as hub
import torch
from qai_hub_models.models.yolov11_det import Model

# 1. Get the model
model = Model.from_pretrained()
# 2. Trace it for TFLite
input_spec = model.get_input_spec()
traced_model = torch.jit.trace(model, model.sample_inputs())

# 3. Submit directly to AI Hub (Bypassing the export script's version checks)
compile_job = hub.submit_compile_job(
    model=traced_model,
    device=hub.Device("Samsung Galaxy S24 Ultra"),
    input_specs=input_spec,
    options="--target_runtime tflite"
)

print(f"Job submitted! Monitor it here: {compile_job.url}")
compile_job.wait()
target_model = compile_job.get_target_model()
target_model.download("detector.tflite")
print("Done! Model saved as detector.tflite")