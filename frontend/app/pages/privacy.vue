<template>
  <NuxtLayout name="legal" title="Privacy Policy" :subtitle="`Last updated: ${lastUpdated}`">
    <p>
      This Privacy Policy explains how oglimmer.com / Oliver Zimpasser ("we", "our", "us") processes
      personal data when you use Renovate Initializr (the "Service"). It applies to all visitors and
      to signed&#8209;in users of the dashboard.
    </p>

    <h2>1. Controller</h2>
    <p>
      Controller within the meaning of Art. 4(7) GDPR:<br>
      Oliver Zimpasser, Berliner Str. 39a, 63110 Rodgau, Germany<br>
      Email: <a href="mailto:oglimmer@gmail.com">oglimmer@gmail.com</a>
    </p>
    <p>
      We have not appointed a Data Protection Officer because we are not required to do so under
      Art. 37 GDPR.
    </p>

    <h2>2. Data We Process</h2>
    <p>We process the following categories of personal data, depending on how you use the Service:</p>
    <ul>
      <li>
        <strong>No data is required to use the core tool.</strong> Building a configuration, the live
        preview and downloading the resulting <code>renovate.json</code> happen entirely in your
        browser and do not require an account or send personal data to us.
      </li>
      <li>
        <strong>Configuration content you submit for review</strong>: when you use the "AI feedback"
        feature or the public REST API, the configuration text you provide is sent to our backend and
        on to our AI provider (see Section 5). A <code>renovate.json</code> is not normally personal
        data, but you control what you paste in.
      </li>
      <li>
        <strong>Account data from your Git host</strong> (only if you sign in to the dashboard): the
        OAuth provider (GitHub or GitLab), your numeric provider user id, your login/username, your
        display name, and your avatar URL. This is stored in our database to recognise you across
        sessions.
      </li>
      <li>
        <strong>OAuth access token</strong> (only while signed in): issued by your Git host and used to
        call its API on your behalf to list your repositories and read their Renovate configuration. To
        do this the Service requests the GitHub <code>repo</code> and <code>read:org</code> scopes, or
        the GitLab <code>read_api</code> scope. These tokens may technically grant broader access than
        the Service uses; the Service only reads repository metadata and Renovate configuration files and
        never creates, modifies or deletes anything in your repositories. The token is held only for the
        duration of your session (refreshed where your provider issues short&#8209;lived tokens) and is not
        stored in our database.
      </li>
      <li>
        <strong>Tracked repositories</strong> (only if you sign in): the full names (e.g.
        <code>owner/repo</code>) of the repositories you choose to track on the dashboard.
      </li>
      <li>
        <strong>Technical log data</strong>: as with any web service, the application server and the
        hosting / ingress infrastructure may record standard technical logs — which can include your IP
        address — for security and troubleshooting. The application itself does not keep a per&#8209;request
        access log, but configuration text you submit for AI review may appear in an error log if the AI
        provider returns an unparseable response. We do not use analytics, device fingerprinting or
        cross&#8209;site tracking.
      </li>
    </ul>

    <h2>3. Whether You Have to Provide Data</h2>
    <p>
      You are not under any statutory or contractual obligation to provide personal data. You can use
      the configuration builder, preview, download and REST API without an account. Signing in is only
      necessary if you want to use the dashboard to compare your repositories' configuration; if you do
      not sign in, that feature is simply unavailable to you.
    </p>

    <h2>4. Purposes and Legal Bases</h2>
    <ul>
      <li>
        <strong>Providing the AI review</strong> (Art. 6(1)(b) GDPR): processing the configuration you
        submit and returning a summary, issues and an improved configuration. This is performed at your
        request.
      </li>
      <li>
        <strong>Dashboard sign&#8209;in and repository comparison</strong> (Art. 6(1)(b) GDPR): authenticating
        you via your Git host, storing your account data and tracked repositories, and using your access
        token to read your repositories' Renovate configuration so the dashboard can display it.
      </li>
      <li>
        <strong>Security, abuse prevention and troubleshooting</strong> (Art. 6(1)(f) GDPR): our
        legitimate interest is to keep the Service running, secure and free of misuse, supported by the
        technical log data described in Section 2.
      </li>
      <li>
        <strong>Compliance with legal obligations</strong> (Art. 6(1)(c) GDPR): where applicable law
        requires us to retain or disclose data.
      </li>
    </ul>
    <p>
      We do not carry out any automated decision&#8209;making, including profiling, that produces legal
      effects or similarly significantly affects you within the meaning of Art. 22 GDPR.
    </p>

    <h2>5. AI-Assisted Review</h2>
    <p>
      The "AI feedback" feature and the REST API call a third&#8209;party AI provider, <strong>DeepSeek</strong>
      (operated by Hangzhou DeepSeek Artificial Intelligence Co., Ltd., People's Republic of China), to
      analyse a configuration. Only the configuration text you submit for that single request is
      transmitted. No account data, access tokens, tracked&#8209;repository lists or server logs are sent.
      This feature is only active when the operator of the instance has configured an AI provider API
      key.
    </p>

    <h2>6. Recipients of Your Data</h2>
    <p>We disclose personal data only to the following categories of recipients:</p>
    <ul>
      <li>
        <strong>Your Git host (GitHub or GitLab)</strong>: when you sign in, you authenticate directly
        with them and we receive your basic profile from them. Your use of their login is also subject to
        their own privacy policy; they act as an independent controller for their platform.
      </li>
      <li>
        <strong>DeepSeek</strong>: receives the configuration text you submit for review (see Section 5),
        acting as a processor / recipient for that request.
      </li>
      <li>
        <strong>Hosting and infrastructure providers</strong> on whose servers this instance of the
        Service is operated; they act as processors under Art. 28 GDPR.
      </li>
    </ul>

    <h2>7. International Transfers</h2>
    <p>
      When you use the AI review feature, the configuration text you submit is transferred to DeepSeek in
      the People's Republic of China, for which the European Commission has not issued an adequacy
      decision. Because the review is carried out only at your explicit request and is necessary to
      provide the feature you asked for, we rely on the derogations in Art. 49(1)(a) and (b) GDPR for
      this transfer. The transfer carries the risks generally associated with transfers to countries
      without an adequacy decision; if you do not wish your configuration to be transferred, simply do
      not use the AI review feature. You can request more information by contacting us (see Section 11).
    </p>

    <h2>8. Cookies and Local Storage</h2>
    <p>
      When you sign in to the dashboard, we set the following strictly necessary cookies:
    </p>
    <ul>
      <li>
        <strong>A session cookie</strong> — keeps you signed in to the dashboard for the duration of
        your session.
      </li>
      <li>
        <code>XSRF-TOKEN</code> — a cross&#8209;site request forgery (CSRF) protection token that the
        frontend reads and echoes back on state&#8209;changing requests.
      </li>
    </ul>
    <p>
      The Service also uses your browser's <code>sessionStorage</code> to hand a configuration over from
      the dashboard to the editor ("Open in editor"); this stays in your browser and is not sent to us.
    </p>
    <p>
      These items are strictly necessary to provide the functionality you have requested and therefore do
      not require consent under § 25 (2) Nr. 2 TDDDG. We do not use analytics, advertising or
      cross&#8209;site tracking cookies of any kind.
    </p>

    <h2>9. Retention and Deletion</h2>
    <ul>
      <li>
        <strong>Submitted configuration content</strong>: processed transiently to produce the review and
        returned to you; we do not persist it in our database after the response is returned. It may
        appear briefly in server logs in error situations.
      </li>
      <li>
        <strong>Account data and tracked repositories</strong>: retained for as long as you use the
        dashboard. You can revoke the Service's access from your Git host at any time, and you can ask us
        to delete your account data and tracked&#8209;repository list (see Section 10 and 11).
      </li>
      <li>
        <strong>OAuth access token</strong>: held only for the duration of your session and discarded
        afterwards; not stored in our database.
      </li>
      <li>
        <strong>Technical / infrastructure logs</strong>: retained for a short period (typically a few
        days) unless we have a concrete reason to keep them longer to investigate an incident.
      </li>
    </ul>

    <h2>10. Security</h2>
    <p>
      We implement appropriate technical and organisational measures within the meaning of Art. 32 GDPR
      to protect your data, including transport encryption (HTTPS), authenticated sessions, CSRF
      protection and minimisation of the data we collect. No online service can guarantee absolute
      security.
    </p>

    <h2>11. Your Rights</h2>
    <p>Under the GDPR you have the following rights regarding your personal data:</p>
    <ul>
      <li>Right of access (Art. 15 GDPR).</li>
      <li>Right to rectification of inaccurate data (Art. 16 GDPR).</li>
      <li>Right to erasure ("right to be forgotten") (Art. 17 GDPR).</li>
      <li>Right to restriction of processing (Art. 18 GDPR).</li>
      <li>Right to data portability for data you have provided to us (Art. 20 GDPR).</li>
    </ul>
    <p>
      <strong>Right to object (Art. 21 GDPR).</strong> You have the right to object, on grounds relating
      to your particular situation, to processing of your personal data that is based on our legitimate
      interests (Art. 6(1)(f) GDPR), in particular the technical&#8209;log processing described in Sections 2
      and 4. If you object, we will stop processing the data in question unless we can demonstrate compelling
      legitimate grounds that override your interests or the processing is needed to establish, exercise
      or defend legal claims.
    </p>
    <p>
      To exercise any of these rights, contact us at
      <a href="mailto:oglimmer@gmail.com">oglimmer@gmail.com</a>. You also have the right to lodge a
      complaint with a supervisory authority. The competent authority for the controller is the
      Hessischer Beauftragter für Datenschutz und Informationsfreiheit, Wiesbaden, Germany; you may also
      lodge a complaint with the supervisory authority of your habitual residence or place of work.
    </p>

    <h2>12. Children</h2>
    <p>
      The Service is not intended for children under 16. Do not sign in or submit content if you are
      under 16.
    </p>

    <h2>13. Changes to this Policy</h2>
    <p>
      We may update this Privacy Policy to reflect changes in our practices or legal requirements. The
      "Last updated" date at the top of this page reflects the effective date of the current version.
    </p>

    <h2>14. Contact</h2>
    <p>
      For privacy questions or requests, contact:
      <a href="mailto:oglimmer@gmail.com">oglimmer@gmail.com</a>.
    </p>
  </NuxtLayout>
</template>

<script setup lang="ts">
useHead({ title: 'Privacy Policy — Renovate Initializr' })
const lastUpdated = '2026-06-21'
</script>
