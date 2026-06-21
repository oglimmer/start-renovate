<template>
  <NuxtLayout name="legal" subtitle="Legal information and contact details.">
    <template #title>Imprint / <em>Impressum</em></template>

    <p>For Germany: Angaben gemäß § 5 DDG (Digitale-Dienste-Gesetz)</p>

    <h2>Diensteanbieter / Website Provider</h2>
    <p>Diese Webseite wird bereitgestellt von / This website is provided by:</p>

    <h3>Oliver Zimpasser</h3>
    <p>
      Berliner Str. 39a<br>
      63110 Rodgau<br>
      Germany
    </p>

    <h2>Kontakt / Contact</h2>
    <p>
      E&#8209;Mail: <a href="mailto:oglimmer@gmail.com">oglimmer@gmail.com</a><br>
      (Angaben zur schnellen elektronischen Kontaktaufnahme und unmittelbaren Kommunikation
      gem. § 5 Abs. 1 Nr. 2 DDG)
    </p>

    <h2>Verantwortlich i.S.d. § 18 Abs. 2 MStV</h2>
    <p>
      Verantwortlich für den Inhalt / Responsible for content:<br>
      Oliver Zimpasser, Berliner Str. 39a, 63110 Rodgau, Germany
    </p>

    <h2>EU&#8209;Streitschlichtung / Dispute Resolution</h2>
    <p>
      Die Europäische Kommission stellt eine Plattform zur Online&#8209;Streitbeilegung (OS) bereit:
      <a href="https://ec.europa.eu/consumers/odr" target="_blank" rel="noopener">https://ec.europa.eu/consumers/odr</a>.
      Wir sind nicht verpflichtet und grundsätzlich nicht bereit, an Streitbeilegungsverfahren vor
      einer Verbraucherschlichtungsstelle teilzunehmen.
    </p>

    <h2>Service Information</h2>
    <p>
      Renovate Initializr is a free, open&#8209;source tool provided at no charge. It is not affiliated
      with or endorsed by Mend.io, the maintainers of Renovate.
    </p>
  </NuxtLayout>
</template>

<script setup lang="ts">
useHead({ title: 'Imprint — Renovate Initializr' })
</script>
