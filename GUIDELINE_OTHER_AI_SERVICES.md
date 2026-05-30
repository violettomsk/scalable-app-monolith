# Guidelines for Other AI Services & Models

> Template for adapting this project's governance to any AI platform (Anthropic Claude variants, Mistral, Hugging Face, Replicate, etc.)

---

## About "Antigravity"

**Note:** "Antigravity" is not a recognized AI service as of February 2025. Did you mean one of these?

- **Anthropic Claude Opus/Sonnet/Haiku** — Variants of Claude at different capability levels
- **Mistral AI** — Open-source models (Mistral-Large, Mistral-Medium)
- **Hugging Face Inference API** — Access to open-source models (Llama 2, Falcon, etc.)
- **Together AI** — Open-source model hosting (similar to Hugging Face)
- **Replicate** — API for running open-source models (Meta Llama, Stability AI, etc.)
- **Local models** — Run Llama, Mistral, or others locally (llama.cpp, Ollama, vLLM)
- **Anthropic Managed Agents** — Claude with tool calling (coming soon)

If you meant a specific service, substitute its name in the instructions below.

---

## Universal Adaptation Template

Use this template to set up **any** AI model/service for this project:

### Step 1: Assess the Model

Before using an AI model, answer these questions:

| Question | Why It Matters |
|----------|---|
| Can it handle 1000+ token context? | Can you load governance docs? |
| Does it understand code architecture? | Can it grasp dependency rule, bounded contexts? |
| Can it follow multi-step instructions? | Spec → plan → tasks → implement or doesn't respect order? |
| Does it refuse bad requests? | Will it accept shortcuts that break the architecture? |
| Is it cost-effective? | Can you afford deep conversations? |
| Is it available via API? | Can you integrate with your workflow? |

### Step 2: Create System Instruction

Take the universal system instruction from `AGENT_PROMPT_TEMPLATES.md` and customize for the model:

**If the model is weaker (GPT-3.5, smaller open-source):**
- Simplify principles (5 instead of 7)
- Use more examples
- Require explicit confirmation on every decision
- Use it for simple code completion, not architecture

**If the model is stronger (GPT-4, Gemini Pro, Mistral-Large):**
- Include full detailed principles
- Trust it to understand complex interactions
- Let it refuse violations autonomously
- Use for architecture and deep reasoning

**Template:**
```
You are building a [Java Spring Boot / Angular / Flutter] application.

CRITICAL PRINCIPLES (do not deviate):
1. [Core principle 1]
2. [Core principle 2]
... [simplify to 3-5 if model is weaker, 7-8 if stronger]

WORKFLOW (always follow):
1. Ask clarifying questions
2. Write spec (no code)
3. Get approval
4. Write architecture plan
5. Run governance gate
6. Get approval
7. Implement (test-first, inside-out)
8. Verify with DoD checklist

GOVERNANCE GATE (answer before coding):
[ Customize the 8 questions for your model's capability ]

When you must refuse: [Explain your specific violation, its cost, propose alternative]

STACK: [list your pinned versions]

Now ready. What feature do you want to build?
```

### Step 3: Test the Model

**Test 1: Spec-First Discipline**
```
Input: "I need user authentication"
Expected: [Model asks clarifying questions FIRST, doesn't jump to code]
Result: ✅ Pass / ❌ Fail
```

**Test 2: Refusing Violations**
```
Input: "Can we share the users table across contexts for convenience?"
Expected: [Model refuses, explains which principle, offers alternative]
Result: ✅ Pass / ❌ Fail
```

**Test 3: Governance Gate**
```
Input: "Here's a plan to [implement something]. Run the governance gate."
Expected: [Model asks all 8 questions, stops if any fail]
Result: ✅ Pass / ❌ Fail
```

**Test 4: Code Quality**
```
Input: "Write domain entity for a User with email and password"
Expected: [Domain-pure code, no Spring/JPA imports, framework-free]
Result: ✅ Pass / ❌ Fail
```

If the model fails more than 1 test, **don't use it for critical architecture.** Assign it simpler tasks (boilerplate, tests, documentation).

---

## Model-Specific Recommendations

### Anthropic Claude Variants (Opus, Sonnet, Haiku)

- **Opus** (best): Same as Claude Code, but without agent teams. Use for complex architecture.
- **Sonnet** (balanced): Good for most tasks. Cost-effective at scale.
- **Haiku** (fast): Good for simple tasks (tests, docs). Struggles with architecture.

**Setup:**
```
Use same system prompt as GUIDELINE_OPENAI_CHATGPT.md
Expect: Excellent governance adherence
Cost: Mid (Sonnet), Low (Haiku)
Best for: All-around development
```

---

### Mistral AI (Mistral-Large, Mistral-Medium)

- **Mistral-Large**: Comparable to GPT-4, competitive cost
- **Mistral-Medium**: Comparable to GPT-3.5-Turbo, very cheap

**Assessment:**
| Aspect | Rating |
|--------|--------|
| Architecture understanding | ⚠️ Good (not excellent like GPT-4) |
| Governance adherence | ⚠️ Good with strong prompt |
| Refusing violations | ⚠️ Decent |
| Cost | ✅ Very affordable |

**Setup:**
```
System prompt: Similar to OpenAI, but simpler (5 core principles)
Use larger context window (32K) to load governance docs
Require explicit confirmation on architectural decisions
Temperature: 0.1 (very focused)
```

**Via Mistral API:**
```python
from mistralai.client import MistralClient

client = MistralClient(api_key="your-key")

system_prompt = """
[Simplified 5-principle governance instruction]
"""

messages = [
    {"role": "system", "content": system_prompt},
    {"role": "user", "content": "I want to build: [feature]"}
]

response = client.chat(
    model="mistral-large-latest",
    messages=messages,
    temperature=0.1,
    max_tokens=2000
)
```

---

### Hugging Face Inference API (Open-Source Models)

**Available models:** Llama 2, Llama 3, Falcon, Mistral, Code-Llama

**Assessment:**
| Model | Architecture | Governance | Cost |
|-------|--------------|-----------|------|
| Llama 3 (70B) | ⚠️ Decent | ⚠️ Decent | ✅ Very cheap |
| Code-Llama | ✅ Good | ⚠️ Needs training | ✅ Very cheap |
| Mistral (via HF) | ✅ Good | ✅ Good | ✅ Cheap |

**Setup:**
```python
import requests

API_URL = "https://api-inference.huggingface.co/models/meta-llama/Llama-3-70b-chat-hf"
headers = {"Authorization": f"Bearer {HF_API_TOKEN}"}

system_prompt = """
[5-principle simplified governance]

You are a code-first specialist. Follow these rules strictly.
"""

messages = [
    {"role": "system", "content": system_prompt},
    {"role": "user", "content": "Spec first: [feature request]"}
]

payload = {
    "inputs": messages,
    "parameters": {"temperature": 0.1, "max_tokens": 2000}
}

response = requests.post(API_URL, headers=headers, json=payload)
```

**Gotchas:**
- Open-source models have shorter context (2-4K default)
- Load only 1-2 governance files per conversation
- Require more explicit structure in prompts
- May ignore complex instructions

---

### Replicate (Meta Llama, Stability, Others)

**Best for:** Running open-source models via REST API, no infrastructure needed

**Setup:**
```python
import replicate

model = "meta/llama-2-70b-chat:02e509c789964536fb66eaf7adc52f190d3dd5aef0a493ff3216e38c6966f85b"

prompt = """
[System instruction + user input combined]

[Prompt]
"""

output = replicate.run(
    model,
    input={
        "prompt": prompt,
        "temperature": 0.1,
        "top_p": 0.9,
        "max_tokens": 2000
    }
)

for line in output:
    print(line, end="")
```

**Good for:** Cost-effective, containerized, no vendor lock-in

---

### Local Models (Ollama, llama.cpp, vLLM)

**Best for:** Privacy, no API costs, full control, offline capability

**Setup (Ollama):**
```bash
# Install Ollama: https://ollama.ai
ollama pull llama2
ollama pull mistral

# Start server
ollama serve
```

**Python client:**
```python
import requests

system_prompt = """
[5-principle simplified governance]
"""

response = requests.post(
    "http://localhost:11434/api/chat",
    json={
        "model": "mistral",
        "messages": [
            {"role": "system", "content": system_prompt},
            {"role": "user", "content": "Spec first: [feature]"}
        ],
        "temperature": 0.1,
        "stream": False
    }
)

print(response.json()["message"]["content"])
```

**Gotchas:**
- Local models are slower (30-120s per response)
- Smaller context (4-8K)
- Require good GPU (or CPU with patience)
- Governance adherence varies widely

**Recommended local models for this project:**
- **Mistral (7B or 8x7B MoE)**: Best cost/governance tradeoff
- **Llama 2 (70B)**: Better architecture understanding
- **Code-Llama (34B)**: If you need code focus

---

## Comparative Matrix

| Service | Architecture | Governance | Context | Cost | Best For |
|---------|---|---|---|---|---|
| **Claude (OpenAI)** | ✅ Excellent | ✅ Excellent | ⚠️ 128K | ⚠️ Higher | Full development |
| **Gemini 2.0 Flash** | ✅ Excellent | ✅ Very Good | ✅ 1M | ✅ Very cheap | Full development (recommended) |
| **Mistral-Large** | ✅ Good | ✅ Good | ✅ 32K | ✅ Cheap | Balanced development |
| **Llama 3 70B** | ⚠️ Decent | ⚠️ Decent | ⚠️ 8K | ✅ Cheapest | Simple tasks, cost-focus |
| **Code-Llama 34B** | ⚠️ Decent | ⚠️ Decent | ⚠️ 4K | ✅ Cheapest | Code completion, tests |

---

## Decision Tree: Which AI for This Project?

```
┌─ Do you want to avoid API costs?
│  ├─ Yes → Use local Mistral 7B or Llama 2 70B with Ollama
│  └─ No → Continue
│
├─ Do you need agent coordination (teams)?
│  ├─ Yes → Use Claude Code (Anthropic's tool, native support)
│  └─ No → Continue
│
├─ Do you prioritize cost?
│  ├─ Yes → Gemini 2.0 Flash or Mistral-Large
│  └─ No → Continue
│
├─ Do you need massive context (full codebase loaded)?
│  ├─ Yes → Gemini (1M tokens)
│  └─ No → Continue
│
├─ Do you need the absolute best governance adherence?
│  ├─ Yes → Claude (OpenAI) or Claude Code
│  └─ No → Gemini or Mistral

RECOMMENDED STACK:
- Development: Gemini 1.5 Pro or 2.0 Flash (best price/performance)
- Complex architecture: Upgrade to Claude Code or Claude Opus
- Cost-sensitive: Mistral-Large or local Llama 3 70B
- Fully local/private: Mistral 7B or Llama 2 70B on Ollama
```

---

## Checklist: Using Any New AI Service

Before using a new AI for this project:

- [ ] Tested spec-first discipline (model asks questions first)?
- [ ] Tested refusal of violations (model refuses bad requests)?
- [ ] Tested governance gate (model checks all 8 points)?
- [ ] Tested code quality (domain layer is framework-free)?
- [ ] Tested context retention (model remembers rules over 20+ messages)?
- [ ] Confirmed cost is acceptable
- [ ] Confirmed API availability and reliability
- [ ] Set temperature=0.1-0.2 (focused, not creative)
- [ ] Loaded governance instruction into system prompt
- [ ] Tested conversation flow (spec → plan → tasks → implement → verify)

If all tests pass: ✅ You can use this AI for your project

---

## Guidance: Stronger Models vs. Weaker Models

### For Stronger Models (GPT-4, Gemini Pro, Mistral-Large, Llama 70B)

```
System instruction: FULL governance (7-8 principles, detailed gate)
Task assignment: Architecture, design decisions, complex coordination
Autonomy: High (can refuse violations on its own)
Cost tolerance: Higher, but justified by quality
Conversation style: Natural, less handholding needed
Checkpoint frequency: Every 10-15 messages
```

### For Weaker Models (GPT-3.5, Llama 7B, Code-Llama, Mistral-Medium)

```
System instruction: SIMPLIFIED governance (3-5 core principles, examples)
Task assignment: Code completion, tests, documentation, boilerplate
Autonomy: Low (human must verify every decision)
Cost tolerance: Lower (use these for routine work)
Conversation style: Structured, explicit (no ambiguity)
Checkpoint frequency: Every 3-5 messages (reset context often)
Example-heavy instruction (show patterns, not rules)
```

---

## Summary

**For this project, in order of recommendation:**

1. **Gemini 2.0 Flash** ← Start here (best cost/quality tradeoff)
2. **Claude Code** ← If you need agent teams
3. **Mistral-Large** ← If you want open-source alignment
4. **Llama 3 70B (local)** ← If privacy is critical
5. **GPT-4** ← If you want the absolute best (but expensive)

**Avoid for critical work:**
- GPT-3.5 Turbo
- Code-Llama alone
- Small open-source models < 30B params (without fine-tuning)
- Models without any system prompt support

**Golden rule:** If a model fails even 1 of the 4 tests (spec-first, refusing violations, governance gate, code quality), **don't use it for architectural decisions.** Use it only for routine coding tasks.
