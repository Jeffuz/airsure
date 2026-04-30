from qai_hub_models.models.yolov11_det import Model
import torch

model = Model.from_pretrained()
sample_input = model.sample_inputs()
output = model(*sample_input)

print(f"Output type: {type(output)}")
if isinstance(output, (list, tuple)):
    print(f"Number of outputs: {len(output)}")
    for i, o in enumerate(output):
        print(f"Output {i} shape: {o.shape}")
elif isinstance(output, torch.Tensor):
    print(f"Output shape: {output.shape}")
else:
    print(f"Output: {output}")
