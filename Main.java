i// Main Class (Spring Boot Entry Point)
// Here’s the main Spring Boot application class:

package com.example.xmlparser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}




import java.sql.*;
import java.io.*;
import java.util.*;
import org.apache.commons.csv.*;

public class MRMExporterFinal {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:clickhouse://<HOST>:<PORT>/<DATABASE>";
        String user = "<USER>";
        String password = "<PASSWORD>";

        // Define common columns
        List<String> commonColumns = Arrays.asList(
            "site", "asset_class", "cob_date", "risk_source_id", "pts_source_id",
            "pts_leg_id", "scenario_set_id", "model", "currency_pair", "job_uuid", "unit"
        );

        try (
            Connection conn = DriverManager.getConnection(url, user, password);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM your_table");
            BufferedWriter irzWriter = new BufferedWriter(new FileWriter("IRZ.csv"));
            BufferedWriter fxWriter = new BufferedWriter(new FileWriter("FXDELTA.csv"));
        ) {
            CSVPrinter irzPrinter = new CSVPrinter(irzWriter,
                    CSVFormat.DEFAULT.withHeader(getIRZHeader(commonColumns)));
            CSVPrinter fxPrinter = new CSVPrinter(fxWriter,
                    CSVFormat.DEFAULT.withHeader(getFXHeader(commonColumns)));

            while (rs.next()) {
                Map<String, String> commonValues = new HashMap<>();
                for (String col : commonColumns) {
                    commonValues.put(col, rs.getString(col));
                }

                // ---- IRZ ----
                String[] irzTenors = getArray(rs, "IRZ.tenor");
                String[] irzValues = getArray(rs, "IRZ.value");
                String[] irzUnits = getArray(rs, "IRZ.unit");
                String[] irzRisks = getArray(rs, "IRZ.risk");
                String[] irzShifts = getArray(rs, "IRZ.shift");

                int irzLen = maxLength(irzTenors, irzValues, irzUnits, irzRisks, irzShifts);
                if (irzLen > 0) {
                    for (int i = 0; i < irzLen; i++) {
                        List<String> row = new ArrayList<>();
                        for (String col : commonColumns) row.add(commonValues.get(col));
                        row.add(safe(irzTenors, i));
                        row.add(safe(irzShifts, i));
                        row.add(safe(irzRisks, i));
                        row.add(safe(irzUnits, i));
                        row.add(safe(irzValues, i));
                        irzPrinter.printRecord(row);
                    }
                }

                // ---- FXDELTA ----
                String[] fxTenors = getArray(rs, "FXDELTA.tenor");
                String[] fxValues = getArray(rs, "FXDELTA.value");
                String[] fxUnits = getArray(rs, "FXDELTA.unit");
                String[] fxRisks = getArray(rs, "FXDELTA.risk");

                int fxLen = maxLength(fxTenors, fxValues, fxUnits, fxRisks);
                if (fxLen > 0) {
                    for (int i = 0; i < fxLen; i++) {
                        List<String> row = new ArrayList<>();
                        for (String col : commonColumns) row.add(commonValues.get(col));
                        row.add(safe(fxTenors, i));
                        row.add(safe(fxRisks, i));
                        row.add(safe(fxUnits, i));
                        row.add(safe(fxValues, i));
                        fxPrinter.printRecord(row);
                    }
                }
            }

            irzPrinter.flush();
            fxPrinter.flush();
        }
    }

    // ----------- Helper Methods ------------

    private static String[] getArray(ResultSet rs, String col) throws SQLException {
        String raw = null;
        try {
            raw = rs.getString(col);
        } catch (SQLException e) {
            return new String[0]; // column not found
        }
        if (raw == null || raw.trim().isEmpty() || raw.equals("[]")) return new String[0];
        raw = raw.replace("[", "").replace("]", "").replace("\"", "");
        return raw.split(",");
    }

    private static String safe(String[] arr, int i) {
        return i < arr.length ? arr[i].trim() : "";
    }

    private static int maxLength(String[]... arrays) {
        int max = 0;
        for (String[] arr : arrays) {
            if (arr != null) max = Math.max(max, arr.length);
        }
        return max;
    }

    private static String[] getIRZHeader(List<String> common) {
        List<String> header = new ArrayList<>(common);
        header.addAll(Arrays.asList("tenor", "shift", "risk", "mrm_unit", "value"));
        return header.toArray(new String[0]);
    }

    private static String[] getFXHeader(List<String> common) {
        List<String> header = new ArrayList<>(common);
        header.addAll(Arrays.asList("tenor", "risk", "mrm_unit", "value"));
        return header.toArray(new String[0]);
    }
}





import java.io.; import java.sql.; import java.util.; import org.apache.commons.csv.;

public class RiskDataExporterFinal {

private static final List<String> COMMON_COLUMNS = Arrays.asList("site", "asset_class", "cob_date", "risk_source_id", "pts_source_id",
        "pts_leg_id", "scenario_set_id", "model", "currency_pair", "job_uuid", "unit");

public static void main(String[] args) throws Exception {
    Connection conn = DriverManager.getConnection("jdbc:your-db-url", "username", "password");
    Statement stmt = conn.createStatement();
    ResultSet rs = stmt.executeQuery("SELECT * FROM your_table");

    CSVPrinter irzPrinter = new CSVPrinter(new FileWriter("irz.csv"),
            CSVFormat.DEFAULT.withHeader(buildHeader(COMMON_COLUMNS, Arrays.asList("tenor", "shift", "risk", "mrmUnit", "value"))));

    CSVPrinter fxPrinter = new CSVPrinter(new FileWriter("fxdelta.csv"),
            CSVFormat.DEFAULT.withHeader(buildHeader(COMMON_COLUMNS, Arrays.asList("column", "tenor", "value", "risk_factor", "unit"))));

    CSVPrinter pvPrinter = new CSVPrinter(new FileWriter("pv.csv"),
            CSVFormat.DEFAULT.withHeader(COMMON_COLUMNS.toArray(new String[0])));

    while (rs.next()) {
        Map<String, String> commonValues = new LinkedHashMap<>();
        for (String col : COMMON_COLUMNS) {
            commonValues.put(col, rs.getString(col));
        }

        boolean hasIRZ = rs.getString("irz.tenor") != null || rs.getString("irz.value") != null;
        boolean hasFX = rs.getString("fxdelta.column") != null || rs.getString("fxdelta.value") != null;

        if (hasIRZ) {
            List<List<String>> tenors = parseNestedArray(rs.getString("irz.tenor"));
            List<List<String>> values = parseNestedArray(rs.getString("irz.value"));
            List<List<String>> units = parseNestedArray(rs.getString("irz.unit"));
            List<List<String>> risks = parseNestedArray(rs.getString("irz.risk"));
            List<List<String>> shifts = parseNestedArray(rs.getString("irz.shift"));

            for (int i = 0; i < tenors.size(); i++) {
                List<String> tList = tenors.get(i);
                List<String> vList = i < values.size() ? values.get(i) : new ArrayList<>();
                List<String> uList = i < units.size() ? units.get(i) : new ArrayList<>();
                List<String> rList = i < risks.size() ? risks.get(i) : new ArrayList<>();
                List<String> sList = i < shifts.size() ? shifts.get(i) : new ArrayList<>();

                for (int j = 0; j < tList.size(); j++) {
                    List<String> row = new ArrayList<>(commonValues.values());
                    row.add(safe(tList, j));
                    row.add(safe(sList, j));
                    row.add(safe(rList, j));
                    row.add(safe(uList, j));
                    row.add(safe(vList, j));
                    irzPrinter.printRecord(row);
                }
            }
        }

        if (hasFX) {
            List<List<String>> columns = parseNestedArray(rs.getString("fxdelta.column"));
            List<List<String>> tenors = parseNestedArray(rs.getString("fxdelta.tenor"));
            List<List<String>> values = parseNestedArray(rs.getString("fxdelta.value"));
            List<List<String>> risks = parseNestedArray(rs.getString("fxdelta.risk_factor"));
            List<List<String>> units = parseNestedArray(rs.getString("fxdelta.unit"));

            for (int i = 0; i < columns.size(); i++) {
                List<String> cList = columns.get(i);
                List<String> tList = i < tenors.size() ? tenors.get(i) : new ArrayList<>();
                List<String> vList = i < values.size() ? values.get(i) : new ArrayList<>();
                List<String> rList = i < risks.size() ? risks.get(i) : new ArrayList<>();
                List<String> uList = i < units.size() ? units.get(i) : new ArrayList<>();

                for (int j = 0; j < cList.size(); j++) {
                    List<String> row = new ArrayList<>(commonValues.values());
                    row.add(safe(cList, j));
                    row.add(safe(tList, j));
                    row.add(safe(vList, j));
                    row.add(safe(rList, j));
                    row.add(safe(uList, j));
                    fxPrinter.printRecord(row);
                }
            }
        }

        if (!hasIRZ && !hasFX) {
            pvPrinter.printRecord(commonValues.values());
        }
    }

    irzPrinter.close();
    fxPrinter.close();
    pvPrinter.close();
    conn.close();
}

private static List<List<String>> parseNestedArray(String input) {
    List<List<String>> result = new ArrayList<>();
    if (input == null || input.trim().isEmpty() || input.equals("[]")) return result;
    input = input.trim().substring(1, input.length() - 1);
    String[] groups = input.split("(?<=),\s*(?=");
    for (String group : groups) {
        group = group.replaceAll("[\"]", "").trim();
        if (!group.isEmpty()) {
            result.add(Arrays.asList(group.split("\s*,\s*")));
        }
    }
    return result;
}

private static String safe(List<String> list, int index) {
    return index < list.size() ? list.get(index).trim() : "";
}

private static String[] buildHeader(List<String> common, List<String> extras) {
    List<String> header = new ArrayList<>(common);
    header.addAll(extras);
    return header.toArray(new String[0]);
}

}




private static List<List<String>> parseNestedArray(String input) {
    try {
        if (input == null || input.trim().isEmpty() || input.equals("[]")) return new ArrayList<>();

        input = input.trim();

        // Fix: remove escaped outer quotes if present
        if ((input.startsWith("\"[") && input.endsWith("]\"")) || 
            (input.startsWith("'[") && input.endsWith("]'"))) {
            input = input.substring(1, input.length() - 1);
        }

        // Handle flat arrays (e.g., ["Bucket", "Bucket"])
        if (!input.contains("[[") && input.startsWith("[") && input.endsWith("]")) {
            List<String> flat = objectMapper.readValue(input, new TypeReference<List<String>>() {});
            List<List<String>> wrapped = new ArrayList<>();
            wrapped.add(flat);
            return wrapped;
        }

        // Handle nested arrays
        return objectMapper.readValue(input, new TypeReference<List<List<String>>>() {});
    } catch (Exception e) {
        e.printStackTrace();
        return new ArrayList<>();
    }
}




private static List<List<String>> parseNestedArray(String input) {
    List<List<String>> result = new ArrayList<>();

    if (input == null || input.trim().isEmpty() || input.equals("[]")) {
        return result;
    }

    input = input.trim();

    // Remove outer quotes if present (e.g. "\"[1D,1W,2W]\"")
    if (input.startsWith("\"") && input.endsWith("\"")) {
        input = input.substring(1, input.length() - 1);
    }

    // Remove outer brackets if present
    if (input.startsWith("[") && input.endsWith("]")) {
        input = input.substring(1, input.length() - 1);
    }

    // Split on comma or whitespace
    String[] tokens = input.split("[,\\s]+");

    List<String> row = new ArrayList<>();
    for (String token : tokens) {
        String clean = token.trim().replaceAll("^\"|\"$", "");
        if (!clean.isEmpty()) {
            row.add(clean);
        }
    }

    if (!row.isEmpty()) {
        result.add(row); // wrap into one row (List<List<String>>)
    }

    return result;
}




private static String safe(List<String> list, int index) {
    return (list != null && index < list.size()) ? list.get(index).trim() : "";
}





System.out.println("FXDELTA.tenor raw: " + rs.getString("FXDELTA.tenor"));
System.out.println("FXDELTA.value raw: " + rs.getString("FXDELTA.value"));
System.out.println("FXDELTA.unit raw: " + rs.getString("FXDELTA.unit"));
System.out.println("FXDELTA.risk_factor raw: " + rs.getString("FXDELTA.risk_factor"));
System.out.println("FXDELTA.shift_type raw: " + rs.getString("FXDELTA.shift_type"));
System.out.println("FXDELTA.greek raw: " + rs.getString("FXDELTA.greek"));



private static List<List<String>> parseNestedArray(String input) {
    List<List<String>> result = new ArrayList<>();

    if (input == null || input.trim().isEmpty() || input.equals("[]")) return result;

    input = input.trim();

    // Remove outer quotes
    if (input.startsWith("\"") && input.endsWith("\"")) {
        input = input.substring(1, input.length() - 1);
    }

    // Remove outer brackets
    if (input.startsWith("[[") && input.endsWith("]]")) {
        input = input.substring(2, input.length() - 2);
    } else if (input.startsWith("[") && input.endsWith("]")) {
        input = input.substring(1, input.length() - 1);
    }

    // Split rows on "],[" safely
    String[] rows = input.split("\,\\s*\");

    for (String row : rows) {
        List<String> parsedRow = new ArrayList<>();

        // remove any remaining quotes/brackets and preserve the original text
        row = row.replaceAll("^\|\$", "").trim();

        if (!row.isEmpty()) {
            // Now split ONLY on true commas NOT inside phrases
            // So we assume comma-separated values are valid
            for (String val : row.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)")) {
                parsedRow.add(val.trim().replaceAll("^\"|\"$", ""));
            }
        }

        result.add(parsedRow);
    }

    return result;
}




if (hasIRZ) {
    List<List<String>> shifts = parseNestedArray(rs.getString("IRZCDELTA.shift_type"));
    List<List<String>> risks = parseNestedArray(rs.getString("IRZCDELTA.risk_factor"));
    List<List<String>> units = parseNestedArray(rs.getString("IRZCDELTA.unit"));
    List<List<List<String>>> values = parseDoubleNestedArray(rs.getString("IRZCDELTA.value"));
    List<List<List<String>>> tenors = parseDoubleNestedArray(rs.getString("IRZCDELTA.tenor"));

    for (int i = 0; i < risks.size(); i++) {
        List<String> riskList = risks.get(i);
        List<String> shiftList = shifts.get(i);
        List<String> unitOuter = units.size() > i ? units.get(i) : new ArrayList<>();
        List<List<String>> valueOuter = values.size() > i ? values.get(i) : new ArrayList<>();
        List<List<String>> tenorOuter = tenors.size() > i ? tenors.get(i) : new ArrayList<>();

        for (int j = 0; j < riskList.size(); j++) {
            String risk = safe(riskList, j);
            String shift = safe(shiftList, j);
            List<String> valueList = valueOuter.size() > j ? valueOuter.get(j) : new ArrayList<>();
            List<String> tenorList = tenorOuter.size() > j ? tenorOuter.get(j) : new ArrayList<>();
            List<String> unitList  = unitOuter.size()  > j ? unitOuter.get(j)  : new ArrayList<>();

            int innerCount = Math.max(valueList.size(), tenorList.size());

            for (int k = 0; k < innerCount; k++) {
                List<String> row = new ArrayList<>(commonValues.values());
                row.add(shift);
                row.add(risk);
                row.add(safe(unitList, k));
                row.add(safe(valueList, k));
                row.add(safe(tenorList, k));
                irzPrinter.printRecord(row);
            }
        }
    }
}





Good [morning/afternoon], everyone.
Today, under the theme of ‘The S-Class Time Machine’, we wanted to take you on a short journey — a reflection on how our tech landscape within MSS has evolved, especially from the eyes of juniors like us. We'll walk through what we used, what we use now, and where we see room to grow.Perfect — thank you for laying it out so clearly! Here's a structured and natural-sounding speech/presentation for your session on "The S-Class Time Machine", based on everything you've said.

You can deliver it as-is or use it as a base to adapt into slides or bullet points if needed:


---

“The S-Class Time Machine” – Presentation Script

Good [morning/afternoon], everyone.
Today, under the theme of ‘The S-Class Time Machine’, we wanted to take you on a short journey — a reflection on how our tech landscape within MSS has evolved, especially from the eyes of juniors like us. We'll walk through what we used, what we use now, and where we see room to grow.


---

Part 1: The Shift from CoCo to Copilot

Let’s begin with one of the earliest tools we interacted with — CoCo.
CoCo was an internal AI assistant, somewhat like ChatGPT, designed for MSS. It gave quick responses, helped us interact with internal systems conversationally, and made certain tasks easier to understand.

However, due to concerns around security, governance, and approval, CoCo was eventually demised. It served as a reminder that in a financial environment, compliance and data safety must come first.

But the need for AI-based assistance never left — and that’s where GitHub Copilot stepped in.
Unlike CoCo, Copilot is integrated within our IDEs, focusing solely on code suggestions, completions, and logic assistance. It’s been incredibly useful, especially for juniors — helping us speed up repetitive coding tasks, understand best practices, and navigate unfamiliar logic more confidently.

While Copilot is a great step forward, we do feel there’s still potential for something more tailored — maybe an internal Copilot trained specifically on GDM systems. An AI assistant that understands our logs, systems, and workflows could really bridge the knowledge gap for new joiners and reduce our dependency on oral or tribal knowledge.


---

Part 2: The Evolution of Deployment Tools

Next, we want to talk about deployment tools — and how that landscape has changed.
Earlier, we had tools like RENDEX, which required us to follow very specific steps with little flexibility or visibility. Over time, there was a shift towards GPT-3 based internal command tools which made things faster and less manual. This was a turning point where the idea of command-driven deployment became more accepted.

In our team specifically, we made a shift from Jenkins to Ansible. Jenkins, although reliable, often became hard to scale — we had to maintain multiple jobs per environment, and logging wasn’t always easy to trace. Ansible gave us a more code-based, YAML-driven approach. It allowed us to write reusable playbooks, automate better, and integrate well with infrastructure.

That said, even with Ansible, we sometimes feel the lack of a centralized interface. Different teams still use different deployment tools or scripts, and that brings inconsistency — especially when you’re trying to debug or collaborate cross-team.


---

Part 3: Looking Forward – A Centralized Future?

Which brings us to something we want to reflect on as juniors —
Wouldn’t it be helpful to have a centralized, standardized deployment platform at least within GDM?
One that all teams follow, with proper access control, logging, audit trails, templates, and monitoring?

And so, we wanted to take this opportunity to ask —
Has there been any discussion at a broader level about standardizing deployment tools within GDM or MSS overall?
If yes, we’d love to understand what some of the blockers or considerations have been. If not, is this something we as teams can move towards in the future?


---

Closing Note:
This journey through past and present tech shows us that while we’ve come a long way, we still have the opportunity to streamline, simplify, and standardize — especially with feedback from the people using these tools daily.

Thank you!


---

Would you like me to convert this into a doc or presentation structure as well? Or help you add your Jenkins-Ansible content when you’re ready?

Thanks, Riya — now that I have the full picture (and Khushbu’s part too), here’s your updated speech in a more casual, easy-to-speak tone, perfectly suited for a study visit or internal session.

I’ve included:

A soft introduction

CoCo to Copilot transition

A connecting line before moving into deployment tools

Your input on Rundeck → GPT-3

Khushbu’s line (based on your earlier reference)

The Ansible vs Jenkins bit

And finally, your closing question to Skevi



---

Speech: The S-Class Time Machine (Simple, Natural Style)

Hi everyone, I’m Riya.
Today under the theme “The S-Class Time Machine”, we wanted to talk about how tech has evolved within MSS — especially from the point of view of juniors like us. We’ll go through what tools we used before, what we use now, and what we wish we could improve.


---

Part 1 – CoCo to Copilot: A Shift in AI Support

Let’s start with something a lot of us were excited about early on — CoCo.
CoCo was like an internal ChatGPT for MSS. You could ask it system-related questions, and it would help you get answers faster. But due to some security and approval issues, it couldn’t be taken forward.

Then came GitHub Copilot, and it’s been a game changer. It sits right inside your code editor and helps you write code faster — it suggests logic, functions, and even fixes. It’s been really helpful, especially for us juniors who are still getting used to the codebase and syntax.


---

Transition Line: The Missing Piece – Deployment Tools

While Copilot has definitely helped us in writing better code, there’s still one area we feel needs work — and that is deployment tools.
This is where we’d like to talk about how things have changed, but also where some challenges still remain.


---

Part 2 – Deployment Tools: From Rundeck to GPT-3 and Beyond

Earlier, teams used tools like Rundeck, which involved clicking through UIs and manually setting parameters. Now, we use more modern, command-based tools — one example is the GPT-3 based internal tool that lets us trigger deployments using specific commands. It’s been faster and more flexible compared to traditional methods.

This is where Khushbu shared her experience, and it really stood out:

> “Earlier, all our endpoints wandered around like confused interns. GPT-3 just walked in and said, ‘Y2, L – route this way.’”



Her line perfectly describes the shift from messy, manual processes to something more streamlined and smart.


---

Part 3 – From Jenkins to Ansible (Insert Your Content Here)

You can now insert your Jenkins vs Ansible comparison here — maybe something like:

> We also saw a shift from Jenkins to Ansible. Jenkins, while stable, required maintaining multiple jobs, and debugging failures was time-consuming. With Ansible, we could manage everything as code, automate more easily, and reduce manual errors.



(If you want, I can refine this further once you give me your points.)


---

Part 4 – Looking Ahead: A Unified Approach

Even though tools have improved, every team still uses different deployment methods. That makes collaboration tricky and increases the learning curve for new joiners.

Which brings us to an idea we really believe in:
Wouldn’t it be great if we had a centralized deployment platform — at least within GDM?
Something that’s secure, reusable, standardized, and easy for everyone to use across teams.


---

Closing Question for Skevi

So we’d love to ask:
Has there ever been a broader-level discussion about standardizing deployment tools across GDM or MSS?
And if yes, what were some of the blockers? Or what factors stopped it from being taken forward?


---

Let me know if you want a Word doc, a bullet version for slides, or help finalizing your Jenkins-Ansible section.

Thanks, Riya! Based on the content in the image you shared, here's the revised version of your presentation section with the CoCo context included accurately, using the official language while still keeping it conversational and easy to present:


---

Updated CoCo to Copilot Section – For Your Speech

Let’s start with something that many of us were excited about — CoCo.
CoCo was an internal tool built to experiment with large language models, kind of like ChatGPT but specifically for MSS. It helped with things like Q&A, quick context fetching, and more conversational interactions with internal systems.

However, over the last year, the control landscape at HSBC changed, and with that came stricter focus on risk frameworks and duplication concerns.
As officially communicated, due to limited risk control and concerns around duplication with more mature capabilities, the LLM Gateway and CoCo were demised.

This reminded all of us that while AI tools are helpful, we must prioritize security, compliance, and centralized governance in everything we build or use — especially when sensitive data is involved.

That said, we didn’t stop there.
We now use GitHub Copilot, which is more secure, approved, and fully integrated with our development environment. Copilot has made day-to-day coding a lot easier for us juniors — it suggests code, logic patterns, and even helps us fix minor issues without having to Google everything.


---

Let me know if you’d like this added into your full speech version, or formatted as slides/bullets too.

Absolutely, Riya! Here's your final polished speech for the Asset Class Time Machine session. It integrates:

A clear introduction

The CoCo to Copilot shift (with accurate info)

A smooth transition into the deployment tools section

The Rundeck to G3 context

Khushbu’s points on Jenkins to Ansible (beautifully blended)

Your final question for Skevi about centralized tools


The tone is natural, clear, and suitable for spoken delivery:


---

“The S-Class Time Machine” – Final Speech (for Riya)

Hi everyone, I’m Riya, and today under the theme ‘The S-Class Time Machine’, I’ll be walking you through how our tech landscape in MSS has evolved — especially from the perspective of us juniors. It’s a journey of what we used earlier, what we use now, and what we feel could be improved going forward.


---

Part 1 – From CoCo to Copilot

Let’s begin with AI assistants.
Earlier, we had a tool called CoCo — an internal assistant built for MSS. It was something like ChatGPT, but specifically designed to help with Q&A, internal tool queries, and more. However, due to risk control limitations and duplication concerns with other LLM tools already available within HSBC, CoCo was demised. This was an important reminder that security, compliance, and proper approvals come first in everything we use.

But the gap left by CoCo didn’t last long. We started using GitHub Copilot, which is officially supported and sits right inside our development environments. Copilot suggests code, logic, and helps reduce the time we spend on repetitive tasks.
It’s been especially helpful for us juniors — making it easier to write clean code and understand complex logic faster.


---

Connecting Thought: Where We Still Struggle

While Copilot has made coding easier, there’s another space where we still face challenges — and that’s deployments.


---

Part 2 – From Rundeck to GPT-3, and Jenkins to Ansible

Previously, tools like Rundeck were used — both as a deployment and scheduling tool. While it worked for some time, there were concerns raised during audits about its compliance, and since it wasn’t CTO-approved, teams had to move away from it.

That’s when internal tools like GPT-3-based command platforms were introduced — allowing us to trigger deployments via commands instead of clicking through UIs. This made things faster, more traceable, and more standardized.

In parallel, we saw a transition from Jenkins to Ansible.
When Khushbu joined the team, Jenkins was in use — but it came with limitations:

It lacked proper validation controls,

The job structure was hard to maintain,

And the user interface wasn’t always intuitive.


With the introduction of Technology Risk and Control Book (TRCB), teams moved to Ansible, and this really improved things.
Ansible allowed for phased deployments, YAML-based playbooks, and a much simpler, cleaner deployment experience. It also ensured better validation, auditability, and reusability — all key when working across environments.

So far, Ansible has streamlined our workflows and made it easier for developers to manage deployments with less risk and better visibility.


---

Part 3 – A Thought for the Future

That said, even now, every team still uses slightly different tools or processes for deployment.
As juniors, this can be confusing. It also makes it harder to collaborate across teams when everyone is using a different method.

Which brings us to a suggestion:
Wouldn’t it be helpful to have a centralized deployment platform within GDM or MSS — something secure, standardized, and easy to use?


---

Closing Question to Skevi

So we’d love to ask:
Has there ever been any discussion at a broader level about standardizing deployment tools across MSS?
If yes, what were some of the blockers that prevented it from being rolled out? And if not, do you think this is something we can work toward?


---

Thank you!


---

Let me know if you want this broken into bullet points for a slide deck or Google Doc version for final review!

