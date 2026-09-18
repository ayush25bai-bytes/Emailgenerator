import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

public class main {
    public static final String RESET = "\u001B[0m";
    public static final String BOLD = "\u001B[1m";
    public static final String DIM = "\u001B[2m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String CYAN = "\u001B[36m";

    public static void main(String[] args) {
        System.out.print(RESET);

        String envPath = ".env";
        Map<String, String> envVars = EnvLoader.loadEnv(envPath);
        String apiKey = envVars.get("GROQ_API_KEY");

        Scanner scanner = new Scanner(System.in);
        
        if (apiKey == null || apiKey.isEmpty()) {
            System.out.println(CYAN + BOLD + "\n+------------------------------------------+" + RESET);
            System.out.println(CYAN + BOLD + "|      AI Email Architect Setup            |" + RESET);
            System.out.println(CYAN + BOLD + "+------------------------------------------+\n" + RESET);
            
            System.out.println(DIM + "[!] GROQ_API_KEY not found in local environment." + RESET);
            System.out.println(DIM + "[!] You can generate a free API key at: " + BOLD + "https://console.groq.com/" + RESET + "\n");
            
            System.out.print(CYAN + "> Please enter your Groq API Key: " + RESET);
            apiKey = scanner.nextLine().trim();
            
            if (apiKey.isEmpty()) {
                System.out.println("\n" + RED + "[X] Error: API Key is required to run this engine. Exiting." + RESET);
                System.exit(1);
            }
            
            try (FileWriter fw = new FileWriter(envPath)) {
                fw.write("GROQ_API_KEY=" + apiKey + "\n");
                System.out.println(GREEN + "[+] Key authenticated and saved to .env for future sessions." + RESET);
            } catch (IOException e) {
                System.out.println(GREEN + "[+] Key authenticated for current session. (Warning: could not save to .env)" + RESET);
            }
        }

        System.out.println("\n" + CYAN + BOLD + "--- EMAIL GENERATOR WIZARD ---" + RESET);
        
        System.out.print(CYAN + "> Your Full Name: " + RESET);
        String senderName = scanner.nextLine().trim();
        
        System.out.print(CYAN + "> Your Email Address: " + RESET);
        String senderEmail = scanner.nextLine().trim();
        
        System.out.print(CYAN + "> Recipient Name: " + RESET);
        String recipientName = scanner.nextLine().trim();
        
        System.out.print(CYAN + "> Recipient Email: " + RESET);
        String recipientEmail = scanner.nextLine().trim();
        
        System.out.print(CYAN + "> CC (optional): " + RESET);
        String cc = scanner.nextLine().trim();
        
        System.out.print(CYAN + "> BCC (optional): " + RESET);
        String bcc = scanner.nextLine().trim();
        
        System.out.print(CYAN + "> Purpose of Email (e.g., Requesting the Marks Report): " + RESET);
        String topicGoal = scanner.nextLine().trim();
        
        System.out.print(CYAN + "> Tone (Professional, Urgent, Casual) [Default: Professional]: " + RESET);
        String tone = scanner.nextLine().trim();
        if (tone.isEmpty()) tone = "Professional";

        if (topicGoal.isEmpty() || recipientName.isEmpty()) {
            System.out.println("\n" + RED + "[X] Error: Purpose and Recipient are required." + RESET);
            System.exit(1);
        }

        EmailGenerator generator = new EmailGenerator(apiKey, ".");
        generator.generate(senderName, senderEmail, recipientName, recipientEmail, cc, bcc, topicGoal, tone);
        
        scanner.close();
    }
}

class EnvLoader {
    public static Map<String, String> loadEnv(String filePath) {
        Map<String, String> envVars = new HashMap<>();
        File f = new File(filePath);
        if (!f.exists()) return envVars; 
        try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String value = parts[1].trim();
                    if (value.startsWith("'") && value.endsWith("'")) value = value.substring(1, value.length() - 1);
                    else if (value.startsWith("\"") && value.endsWith("\"")) value = value.substring(1, value.length() - 1);
                    envVars.put(key, value);
                }
            }
        } catch (IOException e) { }
        return envVars;
    }
}

class EmailMetadata {
    private String subject;
    private String salutation;
    private String closing;

    public EmailMetadata(String subject, String salutation, String closing) {
        this.subject = subject;
        this.salutation = salutation;
        this.closing = closing;
    }
    public String getSubject() { return subject; }
    public String getSalutation() { return salutation; }
    public String getClosing() { return closing; }
}

class Utils {
    public static String unescapeJson(String text) {
        if (text == null) return null;
        text = text.replace("\\n", "\n").replace("\\\"", "\"").replace("\\\\", "\\").replace("\\t", "\t").replace("\\r", "\r");
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < text.length()) {
            char c = text.charAt(i);
            if (c == '\\' && i + 5 < text.length() && text.charAt(i + 1) == 'u') {
                String hex = text.substring(i + 2, i + 6);
                try {
                    int code = Integer.parseInt(hex, 16);
                    sb.append((char) code);
                    i += 6;
                    continue;
                } catch (NumberFormatException e) { }
            }
            sb.append(c);
            i++;
        }
        return sb.toString();
    }

    public static String slugify(String text) {
        if (text == null || text.isEmpty()) return "untitled";
        text = text.toLowerCase();
        text = text.replaceAll("[^\\w\\s-]", "");
        text = text.replaceAll("[\\s_-]+", "-");
        text = text.replaceAll("^-+|-+$", "");
        if (text.length() > 80) text = text.substring(0, 80);
        return text;
    }
}

class EmailRenderer {
    public static void render(List<String> emailLines) {
        System.out.println("\n" + main.DIM + "====================== EMAIL PREVIEW ======================" + main.RESET);
        for (String line : emailLines) {
            String rendered = line.replace("\u2011", "-").replace("\u2013", "-").replace("\u2014", "--")
                                  .replace("\u2018", "'").replace("\u2019", "'").replace("\u201C", "\"")
                                  .replace("\u201D", "\"").replace("\u2026", "...");
            
            if (rendered.startsWith("FROM:") || rendered.startsWith("TO:") || rendered.startsWith("CC:") || rendered.startsWith("BCC:") || rendered.startsWith("SUBJECT:")) {
                String[] parts = rendered.split(":", 2);
                if (parts.length == 2) {
                    rendered = main.CYAN + main.BOLD + parts[0] + ":" + main.RESET + parts[1];
                }
            } else if (rendered.startsWith("─")) {
                rendered = main.DIM + rendered + main.RESET;
            } else if (rendered.startsWith("--")) {
                rendered = main.DIM + rendered + main.RESET;
            } else if (rendered.trim().startsWith("- ")) {
                rendered = rendered.replaceFirst("- ", main.CYAN + "* " + main.RESET);
            }
            
            // Inline Bold
            rendered = rendered.replaceAll("\\*\\*(.*?)\\*\\*", main.BOLD + "$1" + main.RESET);
            
            System.out.println(rendered);
        }
        System.out.println(main.DIM + "===========================================================" + main.RESET + "\n");
    }
}

class GroqClient {
    private String apiKey;
    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";

    public GroqClient(String apiKey) {
        this.apiKey = apiKey;
    }

    public String sendChatCompletion(String prompt, double temperature, int maxTokens) throws Exception {
        URL url = new URL(API_URL);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Authorization", "Bearer " + apiKey);
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);

        String escapedPrompt = prompt.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
        String jsonInputString = "{"
                + "\"model\": \"openai/gpt-oss-120b\","
                + "\"messages\": [{\"role\": \"user\", \"content\": \"" + escapedPrompt + "\"}],"
                + "\"temperature\": " + temperature + ","
                + "\"max_tokens\": " + maxTokens
                + "}";

        try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
            byte[] input = jsonInputString.getBytes("utf-8");
            wr.write(input, 0, input.length);
        }

        int status = con.getResponseCode();
        BufferedReader in;
        if (status > 299) in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
        else in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        
        StringBuilder content = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) content.append(inputLine);
        in.close();
        con.disconnect();

        if (status > 299) throw new Exception(content.toString());

        return extractContentFromJson(content.toString());
    }

    private String extractContentFromJson(String json) {
        String target = "\"content\"";
        int index = json.indexOf(target);
        if (index == -1) return json;
        int colonIndex = json.indexOf(":", index);
        if (colonIndex == -1) return json;
        int startQuote = json.indexOf("\"", colonIndex);
        if (startQuote == -1) return json;
        int end = json.indexOf("\"", startQuote + 1);
        while (end != -1 && json.charAt(end - 1) == '\\') end = json.indexOf("\"", end + 1);
        if (end != -1) return Utils.unescapeJson(json.substring(startQuote + 1, end));
        return json;
    }
}

class EmailGenerator {
    private GroqClient client;
    private String outputDir;

    public EmailGenerator(String apiKey, String outputDir) {
        this.client = new GroqClient(apiKey);
        this.outputDir = outputDir;
    }

    public void generate(String senderName, String senderEmail, String recipientName, String recipientEmail, String cc, String bcc, String topicGoal, String tone) {
        System.out.println("\n" + main.DIM + "--------------------------------------------------------" + main.RESET);
        System.out.println(main.CYAN + "-> Phase 1:" + main.RESET + " Generating Email Metadata...");

        EmailMetadata metadata = null;
        try {
            metadata = getMetadata(topicGoal, recipientName, tone);
            System.out.println(main.GREEN + "   [+] Metadata Extracted" + main.RESET);
        } catch (Exception e) {
            System.out.println(main.RED + "   [!] API Error during metadata phase: " + e.getMessage() + main.RESET);
            System.out.println(main.RED + "   [!] Generation halted." + main.RESET);
            return;
        }
        
        System.out.println(main.CYAN + "-> Phase 2:" + main.RESET + " Drafting Content...");
        String content = "";
        try {
            content = getContent(topicGoal, recipientName, tone);
            System.out.println(main.GREEN + "   [+] Email Synthesized" + main.RESET);
        } catch (Exception e) {
            System.out.println(main.RED + "   [!] API Error during content phase: " + e.getMessage() + main.RESET);
            System.out.println(main.RED + "   [!] Generation halted." + main.RESET);
            return;
        }

        List<String> finalEmail = assembleEmail(senderName, senderEmail, recipientName, recipientEmail, cc, bcc, metadata, content);
        saveEmail(metadata.getSubject(), finalEmail);
        EmailRenderer.render(finalEmail);
    }

    private EmailMetadata getMetadata(String topicGoal, String recipientName, String tone) throws Exception {
        String prompt = "Generate professional email metadata for:\n" +
                        "- Goal: " + topicGoal + "\n" +
                        "- Recipient: " + recipientName + "\n" +
                        "- Tone: " + tone + "\n\n" +
                        "Return ONLY a valid JSON object:\n" +
                        "{\n" +
                        "  \"subject\": \"Clear & professional subject line\",\n" +
                        "  \"salutation\": \"Appropriate opening (e.g., Dear " + recipientName + ":)\",\n" +
                        "  \"closing\": \"Professional sign-off (e.g., Regards, or Sincerely,)\"\n" +
                        "}";
        
        String response = client.sendChatCompletion(prompt, 0.7, 1000);
        
        String subject = extractJsonField(response, "subject");
        String salutation = extractJsonField(response, "salutation");
        String closing = extractJsonField(response, "closing");
        
        if (subject == null || subject.isEmpty()) subject = "Update regarding: " + topicGoal;
        if (salutation == null || salutation.isEmpty()) salutation = "Dear " + recipientName + ",";
        if (closing == null || closing.isEmpty()) closing = "Regards,";

        return new EmailMetadata(subject, salutation, closing);
    }

    private String getContent(String topicGoal, String recipientName, String tone) throws Exception {
        String prompt = "You are an expert communicator. Write the body of an email based on these details:\n\n" +
                        "GOAL: " + topicGoal + "\n" +
                        "RECIPIENT: " + recipientName + "\n" +
                        "TONE: " + tone + "\n\n" +
                        "STRICT RULES:\n" +
                        "1. Directness: Start with the main request or update immediately.\n" +
                        "2. Length: Maximum 2 concise paragraphs.\n" +
                        "3. Formatting: Use plain text. No 'AI-isms' like 'I hope this finds you well.'\n" +
                        "4. Provide ONLY the message body content.";

        return client.sendChatCompletion(prompt, 0.5, 2000).trim();
    }

    private List<String> assembleEmail(String senderName, String senderEmail, String recipientName, String recipientEmail, String cc, String bcc, EmailMetadata metadata, String body) {
        List<String> lines = new ArrayList<>();
        lines.add(String.format("FROM:    %s <%s>", senderName, senderEmail));
        lines.add(String.format("TO:      %s <%s>", recipientName, recipientEmail));
        if (cc != null && !cc.isEmpty()) lines.add(String.format("CC:      %s", cc));
        if (bcc != null && !bcc.isEmpty()) lines.add(String.format("BCC:     %s", bcc));
        
        lines.add(String.format("SUBJECT: %s", metadata.getSubject()));
        lines.add("\n────────────────────────────────────────────────────────\n");
        lines.add(metadata.getSalutation());
        lines.add("\n" + body + "\n");
        lines.add(metadata.getClosing());
        lines.add(senderName.split(" ")[0]); 
        
        lines.add("\n--");
        lines.add(senderName);
        if (senderEmail != null && !senderEmail.isEmpty()) lines.add("Email: " + senderEmail);
        lines.add("Generated on: " + new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));
        
        return lines;
    }

    private void saveEmail(String subject, List<String> lines) {
        File dir = new File(outputDir, "email_drafts");
        if (!dir.exists()) dir.mkdirs();

        String slug = Utils.slugify(subject);
        File file = new File(dir, slug + ".txt");

        try (FileWriter writer = new FileWriter(file)) {
            for (String line : lines) writer.write(line + "\n");

            System.out.println("\n" + main.DIM + "--------------------------------------------------------" + main.RESET);
            System.out.println(main.GREEN + main.BOLD + "[+] DRAFT SAVED SUCCESSFULLY" + main.RESET);
            System.out.println(main.DIM + "    File Path : " + main.RESET + file.getAbsolutePath());
            System.out.println(main.DIM + "--------------------------------------------------------\n" + main.RESET);
            
        } catch (IOException e) {
            System.out.println("\n" + main.RED + "[X] Error saving file to disk: " + e.getMessage() + main.RESET);
        }
    }

    private String extractJsonField(String json, String field) {
        String target = "\"" + field + "\"";
        int index = json.indexOf(target);
        if (index == -1) return "";
        int colonIndex = json.indexOf(":", index);
        if (colonIndex == -1) return "";
        int startQuote = json.indexOf("\"", colonIndex);
        if (startQuote == -1) return "";
        int endQuote = json.indexOf("\"", startQuote + 1);
        while (endQuote != -1 && json.charAt(endQuote - 1) == '\\') endQuote = json.indexOf("\"", endQuote + 1);
        if (endQuote != -1) return Utils.unescapeJson(json.substring(startQuote + 1, endQuote));
        return "";
    }
}
