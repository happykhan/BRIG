# CLI Usage

BRIG ships a separate CLI JAR (`brig-cli.jar`) for running from the command line without the GUI. This is useful for scripting, automation, and headless server environments.

## Basic Usage

```bash
java -jar brig-cli.jar <reference> <sequence_folder>
```

- `<reference>` — Path to the reference sequence (FASTA, GenBank, or EMBL format)
- `<sequence_folder>` — Path to a folder containing query sequences to compare against the reference

BRIG will discover all valid sequence files in the folder, create one ring per file, run BLAST, and render the output image.

## Options

| Option | Description | Default |
|--------|-------------|---------|
| `--output <path>` | Output image file path | `<sequence_folder>/brig_output.png` |
| `--title <text>` | Image title (shown in centre of ring) | Reference filename |
| `--format <fmt>` | Image format: `png`, `jpg`, `svg`, `svgz` | `png` |
| `--gc-content` | Add a GC Content ring | off |
| `--gc-skew` | Add a GC Skew ring | off |
| `--config <path>` | JSON file with settings overrides | none |
| `--help` | Show usage information | |

## Examples

Generate a simple comparison image:

```bash
java -jar brig-cli.jar reference.fna sequences/
```

With GC content/skew rings and a custom title:

```bash
java -jar brig-cli.jar reference.gbk sequences/ \
  --gc-content --gc-skew \
  --title "E. coli O157:H7" \
  --format svg \
  --output my_comparison.svg
```

Using a JSON config file:

```bash
java -jar brig-cli.jar reference.fna sequences/ --config settings.json
```

## JSON Config Format

The `--config` option accepts a JSON file to override default settings. All fields are optional:

```json
{
  "blastOptions": "-evalue 0.001 -num_threads 4",
  "legendPosition": "middle-right",
  "cgview": {
    "height": "4500",
    "width": "4500",
    "backboneRadius": "900"
  },
  "brig": {
    "defaultUpper": "70",
    "defaultLower": "50"
  }
}
```

### Config fields

- **blastOptions** — Custom BLAST command-line options (same as the BLAST options field in the GUI)
- **legendPosition** — Legend placement: `upper-left`, `upper-center`, `upper-right`, `middle-left`, `middle-right`, `lower-left`, `lower-center`, `lower-right`
- **cgview** — CGView image settings (height, width, backboneRadius, etc.)
- **brig** — BRIG settings (defaultUpper and defaultLower identity thresholds)

## Output

BRIG CLI produces:

1. The rendered image in the specified format
2. A `brig_session.xml` file in the output folder, which can be loaded into the BRIG GUI to continue editing
